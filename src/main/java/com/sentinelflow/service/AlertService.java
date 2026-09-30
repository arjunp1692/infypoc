package com.sentinelflow.service;

import com.sentinelflow.ai.NarrativeService;
import com.sentinelflow.domain.AlertStatus;
import com.sentinelflow.domain.Disposition;
import com.sentinelflow.domain.Narrative;
import com.sentinelflow.domain.NormalizedAlert;
import com.sentinelflow.domain.Severity;
import com.sentinelflow.domain.SeverityDecision;
import com.sentinelflow.config.SentinelProperties;
import com.sentinelflow.persistence.AlertEntity;
import com.sentinelflow.persistence.AlertRepository;
import com.sentinelflow.persistence.IdempotencyRecord;
import com.sentinelflow.persistence.IdempotencyRepository;
import com.sentinelflow.triage.AlertNormalizer;
import com.sentinelflow.triage.IdempotencyKeyFactory;
import com.sentinelflow.triage.SeverityRules;
import com.sentinelflow.triage.SuppressionPolicy;
import com.sentinelflow.web.AlertNotFoundException;
import com.sentinelflow.web.AlertResponse;
import com.sentinelflow.web.CorrelationIdFilter;
import com.sentinelflow.web.CreateAlertRequest;
import com.sentinelflow.web.InvalidRequestException;
import com.sentinelflow.web.SummaryResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final int LIST_LIMIT = 200;

    private final AlertRepository repository;
    private final IdempotencyRepository idempotencyKeys;
    private final AlertNormalizer normalizer;
    private final IdempotencyKeyFactory keys;
    private final SeverityRules severityRules;
    private final NarrativeService narratives;
    private final SuppressionPolicy suppressionPolicy;
    private final SentinelProperties properties;

    public AlertService(
            AlertRepository repository,
            IdempotencyRepository idempotencyKeys,
            AlertNormalizer normalizer,
            IdempotencyKeyFactory keys,
            SeverityRules severityRules,
            NarrativeService narratives,
            SuppressionPolicy suppressionPolicy,
            SentinelProperties properties) {
        this.repository = repository;
        this.idempotencyKeys = idempotencyKeys;
        this.normalizer = normalizer;
        this.keys = keys;
        this.severityRules = severityRules;
        this.narratives = narratives;
        this.suppressionPolicy = suppressionPolicy;
        this.properties = properties;
    }

    @Transactional
    public AlertResponse ingest(CreateAlertRequest request, String idempotencyHeader) {
        if (idempotencyHeader != null && idempotencyHeader.trim().length() > 128) {
            throw new InvalidRequestException("Idempotency-Key must be at most 128 characters");
        }
        NormalizedAlert normalized = normalizer.normalize(
                request.source(),
                request.eventType(),
                request.asset(),
                request.timestamp(),
                request.description(),
                request.indicators());
        String key = keys.create(idempotencyHeader, normalized);
        Optional<AlertEntity> existing = findExisting(key);
        if (existing.isPresent()) {
            return toResponse(existing.get(), Disposition.REPLAYED);
        }
        if (suppressionEnabled()) {
            Optional<AlertEntity> folded = fold(normalized, key);
            if (folded.isPresent()) {
                return toResponse(folded.get(), Disposition.SUPPRESSED);
            }
        }
        return create(normalized, key);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list(Severity severity, AlertStatus status, String source, Instant from, Instant to) {
        return search(severity, status, source, from, to).stream()
                .limit(LIST_LIMIT)
                .map(entity -> toResponse(entity, Disposition.FETCHED))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse get(UUID id) {
        return toResponse(require(id), Disposition.FETCHED);
    }

    @Transactional
    public AlertResponse updateStatus(UUID id, AlertStatus status) {
        AlertEntity entity = require(id);
        entity.applyStatus(status);
        log.info("alert.status id={} status={}", entity.getId(), status);
        return toResponse(entity, Disposition.UPDATED);
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary(Severity severity, AlertStatus status, String source, Instant from, Instant to) {
        List<AlertEntity> matches = search(severity, status, source, from, to);
        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity value : Severity.values()) {
            bySeverity.put(value, 0L);
        }
        Map<AlertStatus, Long> byStatus = new EnumMap<>(AlertStatus.class);
        for (AlertStatus value : AlertStatus.values()) {
            byStatus.put(value, 0L);
        }
        long suppressed = 0;
        for (AlertEntity entity : matches) {
            bySeverity.put(entity.getSeverity(), bySeverity.get(entity.getSeverity()) + 1);
            byStatus.put(entity.getStatus(), byStatus.get(entity.getStatus()) + 1);
            if (entity.getOccurrenceCount() > 1) {
                suppressed += entity.getOccurrenceCount() - 1L;
            }
        }
        return new SummaryResponse(matches.size(), names(bySeverity), names(byStatus), suppressed);
    }

    private AlertResponse create(NormalizedAlert normalized, String key) {
        SeverityDecision decision = severityRules.decide(normalized.eventType(), normalized.indicators().size());
        Narrative narrative = narratives.narrate(normalized, decision.severity());
        AlertEntity entity = AlertEntity.create(UUID.randomUUID(), key, normalized, decision, narrative);
        try {
            entity = repository.saveAndFlush(entity);
            idempotencyKeys.saveAndFlush(new IdempotencyRecord(key, entity.getId()));
        } catch (DataIntegrityViolationException ex) {
            AlertEntity winner = findExisting(key).orElseThrow(() -> ex);
            return toResponse(winner, Disposition.REPLAYED);
        }
        log.info("alert.ingest disposition=CREATED id={} severity={} ruleId={}",
                entity.getId(), entity.getSeverity(), entity.getRuleId());
        return toResponse(entity, Disposition.CREATED);
    }

    private Optional<AlertEntity> fold(NormalizedAlert normalized, String key) {
        Duration window = properties.suppression().window();
        List<AlertEntity> candidates = repository.findSuppressionCandidates(
                normalized.asset(),
                normalized.eventType(),
                List.of(AlertStatus.RESOLVED, AlertStatus.FALSE_POSITIVE),
                normalized.timestamp(),
                normalized.timestamp().minus(window));
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        AlertEntity target = candidates.get(0);
        if (!suppressionPolicy.shouldFold(
                true, target.getStatus(), target.getLastSeen(), normalized.timestamp(), window)) {
            return Optional.empty();
        }
        target.recordOccurrence(normalized.timestamp());
        idempotencyKeys.saveAndFlush(new IdempotencyRecord(key, target.getId()));
        log.info("alert.ingest disposition=SUPPRESSED id={} occurrenceCount={}",
                target.getId(), target.getOccurrenceCount());
        return Optional.of(target);
    }

    private Optional<AlertEntity> findExisting(String key) {
        Optional<AlertEntity> direct = repository.findByIdempotencyKey(key);
        if (direct.isPresent()) {
            return direct;
        }
        return idempotencyKeys.findById(key).flatMap(record -> repository.findById(record.getAlertId()));
    }

    private boolean suppressionEnabled() {
        return properties.suppression() != null && properties.suppression().enabled();
    }

    private List<AlertEntity> search(
            Severity severity, AlertStatus status, String source, Instant from, Instant to) {
        String normalizedSource = source == null || source.isBlank()
                ? null
                : source.trim().toLowerCase(Locale.ROOT);
        return repository.search(severity, status, normalizedSource, from, to);
    }

    private AlertEntity require(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    }

    private AlertResponse toResponse(AlertEntity entity, Disposition disposition) {
        return new AlertResponse(
                entity.getId(),
                entity.getSource(),
                entity.getEventType(),
                entity.getEventTimestamp(),
                entity.getAsset(),
                entity.getDescription(),
                entity.getIndicators(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getRuleId(),
                entity.getOccurrenceCount(),
                entity.getFirstSeen(),
                entity.getLastSeen(),
                disposition,
                new AlertResponse.Recommendation(
                        entity.getNarrativeSource(), entity.getSummary(), entity.getRecommendedAction()),
                correlationId());
    }

    private static String correlationId() {
        String value = MDC.get(CorrelationIdFilter.MDC_KEY);
        return value == null ? "" : value;
    }

    private static <E extends Enum<E>> Map<String, Long> names(Map<E, Long> counts) {
        Map<String, Long> named = new java.util.LinkedHashMap<>();
        counts.forEach((key, value) -> named.put(key.name(), value));
        return named;
    }
}
