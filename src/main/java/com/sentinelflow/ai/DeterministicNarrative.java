package com.sentinelflow.ai;

import com.sentinelflow.domain.Narrative;
import com.sentinelflow.domain.NarrativeSource;
import com.sentinelflow.domain.RecommendedAction;
import com.sentinelflow.domain.Severity;
import org.springframework.stereotype.Component;

@Component
public class DeterministicNarrative {

    public Narrative forAlert(String asset, String eventType, Severity severity) {
        return switch (severity) {
            case CRITICAL -> new Narrative(
                    NarrativeSource.DETERMINISTIC,
                    "Critical security event on " + asset + ": " + eventType + ". Immediate containment required.",
                    RecommendedAction.CONTAIN);
            case HIGH -> new Narrative(
                    NarrativeSource.DETERMINISTIC,
                    "High severity event on " + asset + ": " + eventType + ". Investigate promptly.",
                    RecommendedAction.INVESTIGATE);
            case MEDIUM -> new Narrative(
                    NarrativeSource.DETERMINISTIC,
                    "Medium severity event on " + asset + ": " + eventType + ". Monitor and investigate.",
                    RecommendedAction.MONITOR);
            case LOW -> new Narrative(
                    NarrativeSource.DETERMINISTIC,
                    "Informational event on " + asset + ": " + eventType + ".",
                    RecommendedAction.MONITOR);
        };
    }
}
