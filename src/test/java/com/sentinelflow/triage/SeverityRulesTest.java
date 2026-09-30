package com.sentinelflow.triage;

import static org.assertj.core.api.Assertions.assertThat;

import com.sentinelflow.domain.Severity;
import org.junit.jupiter.api.Test;

class SeverityRulesTest {

    private final SeverityRules rules = new SeverityRules();

    @Test
    void criticalTypeWinsBeforeIndicatorCount() {
        var decision = rules.decide("malware", 0);
        assertThat(decision.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(decision.ruleId()).isEqualTo("SEV-CRITICAL-TYPE");
    }

    @Test
    void highTypeWinsBeforeIndicatorCount() {
        var decision = rules.decide("brute_force", 5);
        assertThat(decision.severity()).isEqualTo(Severity.HIGH);
        assertThat(decision.ruleId()).isEqualTo("SEV-HIGH-TYPE");
    }

    @Test
    void threeIndicatorsRaiseInformationalToHigh() {
        var crowded = rules.decide("informational", 3);
        var quiet = rules.decide("informational", 1);
        assertThat(crowded.ruleId()).isEqualTo("SEV-HIGH-INDICATORS");
        assertThat(quiet.severity()).isEqualTo(Severity.LOW);
        assertThat(quiet.ruleId()).isEqualTo("SEV-LOW-INFORMATIONAL");
    }

    @Test
    void unknownTypeIsMedium() {
        var decision = rules.decide("phishing", 1);
        assertThat(decision.severity()).isEqualTo(Severity.MEDIUM);
        assertThat(decision.ruleId()).isEqualTo("SEV-MEDIUM-DEFAULT");
    }
}
