# SF-001 plan — baseline triage

Status: approved before implementation. See `checkpoint-approved.md`.

## Outcome

Ingest, normalize, and persist an alert. Exact replays return the original row. Severity comes from `docs/domain-rules.md`. Callers can fetch, filter, update status, and read a summary. A recommendation is always present. With no API key it is the deterministic template. With a key, model text is accepted only after `NarrativeValidator` passes.

## Domain rules in force

Normalization, idempotency, severity, status, and recommendation. Suppression is out of scope for this story.

## Files the implementer may change

- `pom.xml`
- `.mvn/**`
- `mvnw`, `mvnw.cmd`
- `src/main/java/com/sentinelflow/**` except `triage/SuppressionPolicy.java`
- `src/main/resources/**`
- `Dockerfile`, `docker-compose.yml`, `.env.example`
- `config/checkstyle/checkstyle.xml`

The tester may add:

- `src/test/java/com/sentinelflow/**` except a suppression test
- `src/test/resources/**`
- `docs/examples/**`

## Tests required before acceptance

- Unit: severity order, derived idempotency key, rejected model payload.
- Integration: create then fetch, replay does not duplicate, triage with no API key, validation error carries a correlation id.

## Non-goals

- Suppression and occurrence folding.
- A UI.
- A live call to a public model during tests or the demo.
