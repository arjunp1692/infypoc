package com.sentinelflow.web;

import com.sentinelflow.domain.AlertStatus;
import com.sentinelflow.domain.Disposition;
import com.sentinelflow.domain.Severity;
import com.sentinelflow.service.AlertService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertController {

    private final AlertService alerts;

    public AlertController(AlertService alerts) {
        this.alerts = alerts;
    }

    @PostMapping("/alerts")
    public ResponseEntity<AlertResponse> create(
            @Valid @RequestBody CreateAlertRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        AlertResponse response = alerts.ingest(request, idempotencyKey);
        HttpStatus status = response.disposition() == Disposition.CREATED
                ? HttpStatus.CREATED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/alerts")
    public List<AlertResponse> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return alerts.list(severity, status, source, from, to);
    }

    @GetMapping("/alerts/summary")
    public SummaryResponse summary(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return alerts.summary(severity, status, source, from, to);
    }

    @GetMapping("/alerts/{id}")
    public AlertResponse get(@PathVariable UUID id) {
        return alerts.get(id);
    }

    @PatchMapping("/alerts/{id}/status")
    public AlertResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        return alerts.updateStatus(id, request.status());
    }
}
