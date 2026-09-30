package com.sentinelflow.web;

import com.sentinelflow.domain.AlertStatus;
import com.sentinelflow.domain.Disposition;
import com.sentinelflow.domain.NarrativeSource;
import com.sentinelflow.domain.RecommendedAction;
import com.sentinelflow.domain.Severity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String source,
        String eventType,
        Instant timestamp,
        String asset,
        String description,
        List<String> indicators,
        Severity severity,
        AlertStatus status,
        String ruleId,
        int occurrenceCount,
        Instant firstSeen,
        Instant lastSeen,
        Disposition disposition,
        Recommendation recommendation,
        String correlationId) {

    public record Recommendation(NarrativeSource source, String summary, RecommendedAction action) {
    }
}
