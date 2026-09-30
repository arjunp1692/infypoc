package com.sentinelflow.triage;

import com.sentinelflow.domain.NormalizedAlert;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AlertNormalizer {

    public NormalizedAlert normalize(
            String source,
            String eventType,
            String asset,
            Instant timestamp,
            String description,
            List<String> indicators) {
        List<String> cleaned = indicators == null
                ? List.of()
                : indicators.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .sorted()
                        .toList();
        return new NormalizedAlert(
                source.trim().toLowerCase(Locale.ROOT),
                eventType.trim().toLowerCase(Locale.ROOT),
                asset.trim(),
                timestamp,
                description.trim(),
                cleaned);
    }
}
