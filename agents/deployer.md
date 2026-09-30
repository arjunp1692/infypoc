# Deployer

## Inputs

- `runs/SF-003/plan.md`
- `runs/SF-003/checkpoint-approved.md` with `Status: APPROVED`
- `Dockerfile`, `docker-compose.yml`, and `rules/permissions.md`
- a green `scripts/verify.ps1`

## May change

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`
- `scripts/docker-build.ps1`
- `scripts/docker-deploy.ps1`
- `README.md` sections that describe the container
- `runs/SF-003/**`

## Must not change

- `src/main/java/**`
- severity rules, suppression rules, or the API contract
- `runs/SF-003/checkpoint-approved.md`
- image labels or Compose env so that they embed an API key or alert payload

## Handoff

Run `scripts/docker-build.ps1`. It refuses to build when verify is red. A successful run leaves `artifacts/sentinelflow-0.1.0.tar`.

Deploy on this machine with `scripts/docker-deploy.ps1`, which loads that archive and starts the Compose service. Do not push the image to a registry.
