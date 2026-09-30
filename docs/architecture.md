# Architecture

## Shape

```
HTTP -> AlertController -> AlertService
                         -> AlertNormalizer
                         -> IdempotencyKeyFactory
                         -> SeverityRules
                         -> NarrativeService -> NarrativeValidator
                                             -> DeterministicNarrative
                                             -> NarrativeProvider (optional HTTP)
                         -> AlertRepository (H2 or PostgreSQL)
```

Severity, idempotency, and narrative validation are plain Java. The web tier does not invent domain outcomes.

## Persistence

`AlertEntity` stores the normalized alert, the severity decision, the recommendation, `occurrenceCount`, `firstSeen`, and `lastSeen`. Indicators are an ordered element collection. The first idempotency key is unique on the alert. Later folded payloads store their keys in `idempotency_key` so a replay of a suppressed event does not increment the count.

The default URL is `jdbc:h2:file:./data/sentinelflow`. Tests use an in-memory H2 database. `application-postgres.yml` switches the datasource to PostgreSQL and is not required for the local run.

## Correlation

`CorrelationIdFilter` reads `X-Correlation-Id` or generates a UUID, stores it in the MDC, and returns it on the response. Problem Details include the same id.

## AI boundary

`NarrativeProvider` is the only type that can perform HTTP. `DisabledNarrativeProvider` is selected when the API key is blank. `OpenAiNarrativeProvider` calls an OpenAI-compatible chat-completions URL with a 3 second timeout and one retry. `NarrativeValidator` accepts or rejects the raw text. Rejection keeps the deterministic template.

The prompt file `src/main/resources/prompts/triage.txt` asks for a JSON shape. It is not the control that enforces that shape.

## Trade-offs

- H2 file mode makes `scripts/run.ps1` enough for a reviewer without Docker. A Dockerfile and Compose file are still provided.
- Summary aggregation loads the filtered rows and counts them in memory. That matches the local volume this service targets.
- Replay safety under concurrency uses the unique constraint and a retry read. Suppression, added in SF-002, is transactional on one instance.

## Known limitations

- No UI, authentication, or multi-node suppression lock.
- The optional model call needs a key the operator supplies outside git.
- The container image is built by `scripts/docker-build.ps1` into `artifacts/sentinelflow-0.1.0.tar`. `scripts/verify.ps1` does not require Docker.
