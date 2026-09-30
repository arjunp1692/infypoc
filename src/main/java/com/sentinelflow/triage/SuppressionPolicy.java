package com.sentinelflow.triage;

import com.sentinelflow.domain.AlertStatus;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SuppressionPolicy {

    public boolean shouldFold(
            boolean enabled,
            AlertStatus status,
            Instant lastSeen,
            Instant eventTime,
            Duration window) {
        if (!enabled || window == null || lastSeen == null || eventTime == null) {
            return false;
        }
        if (status == AlertStatus.RESOLVED || status == AlertStatus.FALSE_POSITIVE) {
            return false;
        }
        if (eventTime.isBefore(lastSeen)) {
            return false;
        }
        return !eventTime.isAfter(lastSeen.plus(window));
    }
}
