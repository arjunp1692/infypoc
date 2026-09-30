# SF-003 decisions

## Deployable artifact

The artifact is an image archive, `artifacts/sentinelflow-0.1.0.tar`, from `docker save` of `sentinelflow:0.1.0`. It is gitignored. `scripts/verify.ps1` stays free of Docker so the service can still be checked on a machine with no engine.

## Engine

The image runs as uid 10001. Compose publishes port 8080, leaves `SENTINELFLOW_AI_API_KEY` empty, and stores H2 files on the named volume `sentinelflow-data`. Postgres stays behind the `postgres` profile. There is no registry push.

## Result

`scripts/docker-build.ps1` wrote the archive on 2026-09-30 after Docker Engine 29.8.1 was running. Image user is `app`.
