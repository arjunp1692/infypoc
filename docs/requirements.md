# Requirements

## Problem

A security operations team receives noisy alerts from several sources. SentinelFlow ingests an alert, normalizes it, assigns a deterministic severity, detects exact replays, and returns a short triage recommendation. The service stays usable when no AI provider is configured.

## Constraints

- Backend only. No UI.
- Java 21 and Spring Boot 3.5.
- Default database is file-based H2 so the demo runs without Docker. PostgreSQL is an optional profile.
- Sample data is synthetic. No real alerts or credentials are stored in the repository.
- Assumptions are listed below and in `docs/architecture.md`.

## Functional scope

- Ingest an alert with source, event type, timestamp, asset, description, and indicators.
- Normalize fields and deduplicate exact replays with a documented idempotency key.
- Assign severity with the rules in `docs/domain-rules.md`.
- Optionally ask a model for a summary and a recommended action. The model cannot change severity.
- Query by severity, status, source, and time range.
- Expose one aggregate summary.
- Update status.
- Fail with validation, Problem Details, a correlation id, and a deterministic recommendation when AI is unavailable.

## Stories

- SF-001 delivers ingest, normalization, idempotent replay, deterministic severity, query, status, summary, and narration with a degraded path.
- SF-002 adds configurable suppression of later alerts that share an asset and event type. Occurrence count and last-seen time are retained. The rule is in `docs/domain-rules.md`.

## Quality bar

- At least 3 unit tests and 2 integration tests.
- OpenAPI from springdoc.
- Structured logs with a correlation id.
- Input validation and safe errors.
- AI output validated as untrusted.
- Degraded mode when the AI key is absent.

## Assumptions

- One local process is the deployment target. Cross-instance locking is out of scope.
- The list endpoint returns at most 200 alerts. The summary counts the full filtered set.
- Time-range filters apply to `lastSeen`.
- Asset matching is case-sensitive after trim. Source and event type are compared after lowercasing.
- A blank AI key means the provider is not called.
