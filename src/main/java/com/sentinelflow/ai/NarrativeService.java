package com.sentinelflow.ai;

import com.sentinelflow.config.SentinelProperties;
import com.sentinelflow.domain.Narrative;
import com.sentinelflow.domain.NormalizedAlert;
import com.sentinelflow.domain.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class NarrativeService {

    private static final Logger log = LoggerFactory.getLogger(NarrativeService.class);

    private final SentinelProperties properties;
    private final NarrativeProvider provider;
    private final NarrativeValidator validator;
    private final DeterministicNarrative deterministic;
    private final String template;

    @Autowired
    public NarrativeService(
            SentinelProperties properties,
            NarrativeProvider provider,
            NarrativeValidator validator,
            DeterministicNarrative deterministic,
            @Value("classpath:prompts/triage.txt") Resource prompt) {
        this(properties, provider, validator, deterministic, read(prompt));
    }

    public NarrativeService(
            SentinelProperties properties,
            NarrativeProvider provider,
            NarrativeValidator validator,
            DeterministicNarrative deterministic,
            String template) {
        this.properties = properties;
        this.provider = provider;
        this.validator = validator;
        this.deterministic = deterministic;
        this.template = template;
    }

    public Narrative narrate(NormalizedAlert alert, Severity severity) {
        Narrative fallback = deterministic.forAlert(alert.asset(), alert.eventType(), severity);
        if (!properties.ai().configured()) {
            return fallback;
        }
        String prompt = template
                .replace("{severity}", severity.name())
                .replace("{eventType}", alert.eventType())
                .replace("{asset}", alert.asset())
                .replace("{description}", alert.description());
        return provider.complete(prompt)
                .map(raw -> acceptOrFallback(raw, severity, fallback))
                .orElse(fallback);
    }

    private Narrative acceptOrFallback(String raw, Severity severity, Narrative fallback) {
        NarrativeValidator.ValidationResult result = validator.validate(raw, severity);
        if (!result.accepted()) {
            log.warn("ai.narrative.rejected reason={}", result.reason());
            return fallback;
        }
        return result.narrative();
    }

    private static String read(Resource prompt) {
        try {
            return prompt.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("triage prompt is missing", ex);
        }
    }
}
