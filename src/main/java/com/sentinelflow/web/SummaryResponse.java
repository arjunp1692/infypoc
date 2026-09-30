package com.sentinelflow.web;

import java.util.Map;

public record SummaryResponse(
        long total,
        Map<String, Long> bySeverity,
        Map<String, Long> byStatus,
        long suppressedOccurrences) {
}
