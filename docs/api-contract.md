# API contract

Base URL: `http://localhost:8080`

OpenAPI: `/v3/api-docs`. Swagger UI: `/swagger-ui.html`.

Every response includes header `X-Correlation-Id`. Error bodies are `application/problem+json`.

## POST /alerts

Creates an alert or returns the original row when the idempotency key matches.

Headers:

- `Idempotency-Key` optional, 1 to 128 characters. When present it is the key. When absent the key is derived as documented in `docs/domain-rules.md`.
- `X-Correlation-Id` optional.

Body:

```json
{
  "source": "edr",
  "eventType": "malware",
  "timestamp": "2026-09-29T12:00:00Z",
  "asset": "host-42",
  "description": "Suspicious process started",
  "indicators": ["ip:203.0.113.10"]
}
```

Required: `source`, `eventType`, `timestamp`, `asset`, `description`. `indicators` may be empty or omitted. Length limits: source 64, event type 64, asset 256, description 4000, at most 50 indicators, each at most 512 characters.

`201` when `disposition` is `CREATED`. `200` when `disposition` is `REPLAYED` or `SUPPRESSED`.

```json
{
  "id": "6f1c0c3e-1b3a-4a1e-9c2a-0b5d5c2a7f10",
  "source": "edr",
  "eventType": "malware",
  "timestamp": "2026-09-29T12:00:00Z",
  "asset": "host-42",
  "description": "Suspicious process started",
  "indicators": ["ip:203.0.113.10"],
  "severity": "CRITICAL",
  "status": "OPEN",
  "ruleId": "SEV-CRITICAL-TYPE",
  "occurrenceCount": 1,
  "firstSeen": "2026-09-29T12:00:00Z",
  "lastSeen": "2026-09-29T12:00:00Z",
  "disposition": "CREATED",
  "recommendation": {
    "source": "DETERMINISTIC",
    "summary": "Critical security event on host-42: malware. Immediate containment required.",
    "action": "CONTAIN"
  },
  "correlationId": "3f0b1c2a-7e11-4d2a-9a10-6c2e1b0a9f77"
}
```

`severity` is `CRITICAL`, `HIGH`, `MEDIUM`, or `LOW`. `status` starts as `OPEN`. `recommendation.action` is `INVESTIGATE`, `CONTAIN`, `MONITOR`, or `ESCALATE`. `recommendation.source` is `DETERMINISTIC` or `AI`.

`SUPPRESSED` means a later alert for the same asset and event type was folded into this row. `occurrenceCount` and `lastSeen` reflect that fold. `firstSeen` stays on the original event.

## GET /alerts

Query parameters, all optional: `severity`, `status`, `source`, `from`, `to`. `from` and `to` are inclusive instants compared to `lastSeen`. `source` is matched after normalization. Response is a JSON array, newest `lastSeen` first, capped at 200.

## GET /alerts/{id}

`200` with the same object shape and `disposition` `FETCHED`. `404` when the id is missing.

## PATCH /alerts/{id}/status

```json
{ "status": "ACKNOWLEDGED" }
```

Allowed status values: `OPEN`, `ACKNOWLEDGED`, `IN_PROGRESS`, `RESOLVED`, `FALSE_POSITIVE`. `200` returns the alert with disposition `UPDATED`. Unknown status or a missing id follows the error format below.

## GET /alerts/summary

Accepts the same filters as `GET /alerts` and is not capped at 200.

```json
{
  "total": 1,
  "bySeverity": { "CRITICAL": 1, "HIGH": 0, "MEDIUM": 0, "LOW": 0 },
  "byStatus": { "OPEN": 1, "ACKNOWLEDGED": 0, "IN_PROGRESS": 0, "RESOLVED": 0, "FALSE_POSITIVE": 0 },
  "suppressedOccurrences": 0
}
```

`suppressedOccurrences` is the sum of `occurrenceCount - 1` across matching alerts. Exact replays do not add to it. Folded repeats do.

## Errors

`400` for validation, malformed JSON, or an unknown status. `404` for an unknown id. `500` for an unexpected failure, with a fixed detail string and no stack trace.

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/alerts",
  "correlationId": "3f0b1c2a-7e11-4d2a-9a10-6c2e1b0a9f77",
  "errors": [{ "field": "source", "message": "must not be blank" }]
}
```
