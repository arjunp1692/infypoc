package com.sentinelflow.triage;

import com.sentinelflow.domain.Severity;
import com.sentinelflow.domain.SeverityDecision;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SeverityRules {

    private static final Set<String> CRITICAL = Set.of(
            "ransomware", "malware", "data_exfiltration", "privilege_escalation");
    private static final Set<String> HIGH = Set.of(
            "brute_force", "suspicious_login", "policy_violation");

    public SeverityDecision decide(String eventType, int indicatorCount) {
        if (CRITICAL.contains(eventType)) {
            return new SeverityDecision(Severity.CRITICAL, "SEV-CRITICAL-TYPE");
        }
        if (HIGH.contains(eventType)) {
            return new SeverityDecision(Severity.HIGH, "SEV-HIGH-TYPE");
        }
        if (indicatorCount >= 3) {
            return new SeverityDecision(Severity.HIGH, "SEV-HIGH-INDICATORS");
        }
        if ("informational".equals(eventType)) {
            return new SeverityDecision(Severity.LOW, "SEV-LOW-INFORMATIONAL");
        }
        return new SeverityDecision(Severity.MEDIUM, "SEV-MEDIUM-DEFAULT");
    }
}
