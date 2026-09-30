# SentinelFlow

Resilient security event triage, delivered through the repository AI delivery framework in [AGENTS.md](AGENTS.md).

The service ingests an alert, normalizes it, assigns a deterministic severity, folds exact replays, suppresses repeated alerts from the same asset and event type, and returns a short recommendation. It stays usable when no AI key is configured.

## Setup

Requirements: Java 21 and Git. Maven is the wrapper in this repository. Docker is optional.

```
powershell -File scripts/run.ps1
```

The script selects a Temurin 21 JDK when one is installed under `C:\Program Files\Eclipse Adoptium`, then starts the service on port 8080. Data is stored in `./data/sentinelflow` (gitignored).

Copy `.env.example` to `.env` only if you want a model call. Leave `SENTINELFLOW_AI_API_KEY` empty for the default path. The demo does not need a key.

Sample calls are in [docs/examples/requests.md](docs/examples/requests.md). OpenAPI is at `http://localhost:8080/v3/api-docs` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

PostgreSQL is `spring.profiles.active=postgres` with [src/main/resources/application-postgres.yml](src/main/resources/application-postgres.yml). `docker compose --profile postgres up` starts a local database for that profile. The default run does not use it.

## Container

The deployer role is [agents/deployer.md](agents/deployer.md). A first Docker Desktop install enables WSL 2 and Hyper-V, then Windows must be restarted before the engine answers. With Docker Desktop running:

```
powershell -File scripts/docker-build.ps1
```

That runs `scripts/verify.ps1`, builds `sentinelflow:0.1.0`, and writes `artifacts/sentinelflow-0.1.0.tar`. The tar is gitignored. Load and start it with:

```
powershell -File scripts/docker-deploy.ps1
```

The image does not contain an API key. Port 8080 is published. Stop it with `docker compose down`.

## Gates

```
powershell -File scripts/verify.ps1
```

That command builds, tests, runs Checkstyle, and scans for secrets. A failing gate blocks acceptance. Prompt text is not a gate.

## Assumptions

- One local process. Suppression is transactional on that process, not a multi-node lock.
- Severity comes only from [docs/domain-rules.md](docs/domain-rules.md). A model may suggest a summary and an action. `NarrativeValidator` rejects anything else.
- `GET /alerts` returns at most 200 rows. `GET /alerts/summary` counts the full filtered set in memory.
- Time filters use `lastSeen`.
- Sample data is synthetic (`host-42`, `example.com`, `203.0.113.0/24`).

## Architecture

HTTP enters `AlertController`. `AlertService` normalizes the payload, derives or accepts an idempotency key, applies `SeverityRules`, then asks `NarrativeService` for a recommendation. With a blank API key the provider is not called. With a key, `OpenAiNarrativeProvider` calls an OpenAI-compatible chat endpoint with a 3 second timeout and one retry. Invalid model JSON is discarded and the deterministic template is stored.

Suppression is a separate step from replay. A new idempotency key for the same asset and event type, inside `sentinelflow.suppression.window` (default 15 minutes), updates the open row: same id, `occurrenceCount + 1`, new `lastSeen`.

## Trade-offs

- H2 file mode is the one-command path because Docker was not available on the authoring machine. The Dockerfile and Compose file are still in the repo.
- Summary counts are computed in memory. That fits the local volume this service targets.
- The prompt asks for a JSON shape. The validator is the control that actually accepts or rejects it.
- Folded events keep the original description and indicators. The new occurrence is counted, not merged into the text.

## Known limitations

- No UI and no authentication.
- No live model call is made by the tests or the demo script.
- `scripts/verify.ps1` does not start Docker. The image archive is produced only by `scripts/docker-build.ps1`.
- Two instances can both fold the same open alert. The idempotency key itself remains unique.

## Delivery record

Stories move through `runs/`. SF-001 is the baseline. SF-002 is the suppression proof task. The 10-minute walkthrough is [docs/demo-plan.md](docs/demo-plan.md).
