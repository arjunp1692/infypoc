package com.sentinelflow.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record CreateAlertRequest(
        @NotBlank @Size(max = 64) String source,
        @NotBlank @Size(max = 64) String eventType,
        @NotNull Instant timestamp,
        @NotBlank @Size(max = 256) String asset,
        @NotBlank @Size(max = 4000) String description,
        @Size(max = 50) List<@Size(max = 512) String> indicators) {
}
