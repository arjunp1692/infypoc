# SF-001 decisions

- Java 21 and Spring Boot 3.5.6. The machine already had JDKs 25 and 26. Spring Boot 3.5 does not support 26, and the assignment recommends 21, so Temurin 21 was installed.
- H2 file database at `./data/sentinelflow` so `scripts/run.ps1` works without Docker. Dockerfile and Compose remain for a reviewer who has Docker.
- Severity is a pure function in `SeverityRules`. The model is not given a path that can overwrite it.
- Idempotency prefers the `Idempotency-Key` header and otherwise hashes the normalized source, event type, asset, timestamp, and sorted indicators.
- List responses are capped at 200. Summary reads the full filtered set.
- Alert descriptions and indicators are not written to logs.
