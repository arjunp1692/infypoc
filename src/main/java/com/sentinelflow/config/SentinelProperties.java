package com.sentinelflow.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinelflow")
public record SentinelProperties(Ai ai, Suppression suppression) {

    public record Ai(String baseUrl, String apiKey, String model, Duration timeout, int maxSummaryLength) {

        public boolean configured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public record Suppression(boolean enabled, Duration window) {
    }
}
