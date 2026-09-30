package com.sentinelflow.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.config.SentinelProperties;
import com.sentinelflow.domain.NarrativeSource;
import com.sentinelflow.domain.NormalizedAlert;
import com.sentinelflow.domain.RecommendedAction;
import com.sentinelflow.domain.Severity;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NarrativeValidatorTest {

    private final NarrativeValidator validator = new NarrativeValidator(new ObjectMapper(), 400);

    @Test
    void rejectsTheRecordedModelPayload() throws Exception {
        String raw = new String(
                getClass().getResourceAsStream("/ai/rejected-narrative.json").readAllBytes(),
                StandardCharsets.UTF_8);
        NarrativeValidator.ValidationResult result = validator.validate(raw, Severity.CRITICAL);
        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).contains("severity");
    }

    @Test
    void rejectsAnActionOutsideTheAllowList() {
        String raw = "{\"summary\":\"Look at the host.\",\"recommendedAction\":\"DELETE_THE_ALERT\"}";
        NarrativeValidator.ValidationResult result = validator.validate(raw, Severity.HIGH);
        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).contains("recommendedAction");
    }

    @Test
    void acceptsJsonThatKeepsTheDeterministicSeverity() {
        String raw = "{\"summary\":\"Contain host-42.\",\"recommendedAction\":\"CONTAIN\",\"severity\":\"CRITICAL\"}";
        NarrativeValidator.ValidationResult result = validator.validate(raw, Severity.CRITICAL);
        assertThat(result.accepted()).isTrue();
        assertThat(result.narrative().source()).isEqualTo(NarrativeSource.AI);
        assertThat(result.narrative().action()).isEqualTo(RecommendedAction.CONTAIN);
    }

    @Test
    void serviceFallsBackWhenTheModelIsRejected() throws Exception {
        String raw = new String(
                getClass().getResourceAsStream("/ai/rejected-narrative.json").readAllBytes(),
                StandardCharsets.UTF_8);
        NarrativeProvider provider = prompt -> Optional.of(raw);
        NarrativeService service = new NarrativeService(
                configured(),
                provider,
                validator,
                new DeterministicNarrative(),
                "severity {severity}");
        var narrative = service.narrate(sample(), Severity.CRITICAL);
        assertThat(narrative.source()).isEqualTo(NarrativeSource.DETERMINISTIC);
        assertThat(narrative.action()).isEqualTo(RecommendedAction.CONTAIN);
    }

    @Test
    void serviceDoesNotCallTheProviderWithoutAKey() {
        NarrativeProvider provider = prompt -> {
            throw new AssertionError("provider must not be called");
        };
        SentinelProperties properties = new SentinelProperties(
                new SentinelProperties.Ai("http://127.0.0.1:9", "", "none", Duration.ofSeconds(3), 400),
                new SentinelProperties.Suppression(true, Duration.ofMinutes(15)));
        NarrativeService service = new NarrativeService(
                properties, provider, validator, new DeterministicNarrative(), "unused");
        var narrative = service.narrate(sample(), Severity.LOW);
        assertThat(narrative.source()).isEqualTo(NarrativeSource.DETERMINISTIC);
        assertThat(narrative.action()).isEqualTo(RecommendedAction.MONITOR);
    }

    private static SentinelProperties configured() {
        return new SentinelProperties(
                new SentinelProperties.Ai("http://127.0.0.1:9", "test-key", "none", Duration.ofSeconds(3), 400),
                new SentinelProperties.Suppression(true, Duration.ofMinutes(15)));
    }

    private static NormalizedAlert sample() {
        return new NormalizedAlert(
                "edr",
                "malware",
                "host-42",
                Instant.parse("2026-09-29T12:00:00Z"),
                "lab event",
                List.of("ip:203.0.113.10"));
    }
}
