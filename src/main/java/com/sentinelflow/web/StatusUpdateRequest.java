package com.sentinelflow.web;

import com.sentinelflow.domain.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull AlertStatus status) {
}
