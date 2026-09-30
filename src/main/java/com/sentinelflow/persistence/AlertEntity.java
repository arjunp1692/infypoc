package com.sentinelflow.persistence;

import com.sentinelflow.domain.AlertStatus;
import com.sentinelflow.domain.Narrative;
import com.sentinelflow.domain.NarrativeSource;
import com.sentinelflow.domain.NormalizedAlert;
import com.sentinelflow.domain.RecommendedAction;
import com.sentinelflow.domain.Severity;
import com.sentinelflow.domain.SeverityDecision;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "alert")
public class AlertEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 256)
    private String asset;

    @Column(nullable = false, length = 4000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alert_indicator", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "indicator", nullable = false, length = 512)
    @OrderColumn(name = "position")
    private List<String> indicators = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertStatus status;

    @Column(nullable = false, length = 64)
    private String ruleId;

    @Column(nullable = false)
    private int occurrenceCount;

    @Column(nullable = false)
    private Instant eventTimestamp;

    @Column(nullable = false)
    private Instant firstSeen;

    @Column(nullable = false)
    private Instant lastSeen;

    @Column(nullable = false, length = 400)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NarrativeSource narrativeSource;

    protected AlertEntity() {
    }

    public static AlertEntity create(
            UUID id,
            String idempotencyKey,
            NormalizedAlert alert,
            SeverityDecision decision,
            Narrative narrative) {
        AlertEntity entity = new AlertEntity();
        entity.id = id;
        entity.idempotencyKey = idempotencyKey;
        entity.source = alert.source();
        entity.eventType = alert.eventType();
        entity.asset = alert.asset();
        entity.description = alert.description();
        entity.indicators = new ArrayList<>(alert.indicators());
        entity.severity = decision.severity();
        entity.ruleId = decision.ruleId();
        entity.status = AlertStatus.OPEN;
        entity.occurrenceCount = 1;
        entity.eventTimestamp = alert.timestamp();
        entity.firstSeen = alert.timestamp();
        entity.lastSeen = alert.timestamp();
        entity.summary = narrative.summary();
        entity.recommendedAction = narrative.action();
        entity.narrativeSource = narrative.source();
        return entity;
    }

    public void applyStatus(AlertStatus next) {
        this.status = next;
    }

    public void recordOccurrence(Instant seen) {
        this.occurrenceCount = this.occurrenceCount + 1;
        this.lastSeen = seen;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAsset() {
        return asset;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getIndicators() {
        return List.copyOf(indicators);
    }

    public Severity getSeverity() {
        return severity;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getRuleId() {
        return ruleId;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public String getSummary() {
        return summary;
    }

    public RecommendedAction getRecommendedAction() {
        return recommendedAction;
    }

    public NarrativeSource getNarrativeSource() {
        return narrativeSource;
    }
}
