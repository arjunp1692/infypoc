# Command permissions

## Allow

- `scripts/build.ps1`, `scripts/test.ps1`, `scripts/lint.ps1`, `scripts/secret-scan.ps1`, `scripts/verify.ps1`, `scripts/run.ps1`
- `scripts/docker-build.ps1`, `scripts/docker-deploy.ps1`
- `docker build`, `docker save`, `docker load`, `docker compose up`, `docker compose down` for the local `sentinelflow` image
- `mvnw.cmd` with `test`, `verify`, `package`, `checkstyle:check`, or `spring-boot:run`
- `git status`, `git diff`, `git log`, `git add`, `git commit`
- HTTP calls to `localhost` for the demo

## Deny

- `git push --force`, `git reset --hard`, and any history rewrite
- committing `.env`, `*.pem`, `*.key`, or files that match `scripts/secret-scan.ps1`
- sending alert descriptions or indicators to a public AI endpoint
- editing `runs/<id>/checkpoint-approved.md` to invent an approval
- skipping tests or Checkstyle to claim the gate passed
- printing the AI API key in logs, docs, or run records
- `docker push`, and baking an API key or alert payload into an image
