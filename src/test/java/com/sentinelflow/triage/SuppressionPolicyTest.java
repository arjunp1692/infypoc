package com.sentinelflow.triage;

import static org.assertj.core.api.Assertions.assertThat;

import com.sentinelflow.domain.AlertStatus;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SuppressionPolicyTest {

    private final SuppressionPolicy policy = new SuppressionPolicy();
    private final Instant lastSeen = Instant.parse("2026-09-29T12:00:00Z");
    private final Duration window = Duration.ofMinutes(15);

    @Test
    void foldsALaterEventInsideTheWindow() {
        assertThat(policy.shouldFold(true, AlertStatus.OPEN, lastSeen, lastSeen.plusSeconds(60), window)).isTrue();
        assertThat(policy.shouldFold(true, AlertStatus.ACKNOWLEDGED, lastSeen, lastSeen.plus(window), window)).isTrue();
    }

    @Test
    void doesNotFoldOutsideTheWindowOrWhenDisabled() {
        assertThat(policy.shouldFold(true, AlertStatus.OPEN, lastSeen, lastSeen.plus(window).plusSeconds(1), window))
                .isFalse();
        assertThat(policy.shouldFold(false, AlertStatus.OPEN, lastSeen, lastSeen.plusSeconds(60), window)).isFalse();
    }

    @Test
    void doesNotFoldAResolvedAlertOrAnOlderEvent() {
        assertThat(policy.shouldFold(true, AlertStatus.RESOLVED, lastSeen, lastSeen.plusSeconds(60), window)).isFalse();
        assertThat(policy.shouldFold(true, AlertStatus.FALSE_POSITIVE, lastSeen, lastSeen.plusSeconds(60), window))
                .isFalse();
        assertThat(policy.shouldFold(true, AlertStatus.OPEN, lastSeen, lastSeen.minusSeconds(1), window)).isFalse();
    }
}
