# SF-003 security review

Status: PASS

- Compose sets `SENTINELFLOW_AI_API_KEY` to an empty value. No key and no alert payload are copied into the image. `.dockerignore` excludes `.env`, `data`, `docs`, `runs`, and `scripts`.
- The runtime stage uses uid 10001, not root.
- `rules/permissions.md` denies `docker push` and denies baking a key or alert payload into an image.
- The Postgres profile password is the local placeholder `local-only`. The secret scan stayed clean.
- The built image `sentinelflow:0.1.0` runs as user `app` with entrypoint `java -jar app.jar`. The archive is `artifacts/sentinelflow-0.1.0.tar`.
