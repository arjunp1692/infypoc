package com.sentinelflow.domain;

import java.time.Instant;
import java.util.List;

public record NormalizedAlert(
        String source,
        String eventType,
        String asset,
        Instant timestamp,
        String description,
        List<String> indicators) {
}
