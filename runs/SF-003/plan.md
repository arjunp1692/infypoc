# SF-003 plan — container build and deploy artifact

Status: approved in this session. See `checkpoint-approved.md`.

## Outcome

Add a deployer role that builds the SentinelFlow image and writes a loadable image archive. Product behavior does not change.

The deployable artifact is `artifacts/sentinelflow-0.1.0.tar`, produced by `docker save` after `docker build -t sentinelflow:0.1.0`.

## Rule

`scripts/verify.ps1` must pass before the image build. The image must not contain `.env`, alert samples from a live system, or an API key. `SENTINELFLOW_AI_API_KEY` stays empty in Compose.

## Files the deployer may change

- `agents/deployer.md` (new)
- `AGENTS.md`
- `rules/permissions.md`
- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore` (new)
- `scripts/docker-build.ps1` (new)
- `scripts/docker-deploy.ps1` (new)
- `.gitignore`
- `README.md`
- `docs/architecture.md`
- `runs/SF-003/**`

## Tests

No new Java tests. The story gate is `scripts/docker-build.ps1` after `scripts/verify.ps1`.

## Non-goals

- A public registry push.
- Changing severity, suppression, or the API.
- Requiring Docker for `scripts/verify.ps1`.
