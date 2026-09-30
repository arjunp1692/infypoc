# SF-003 execution log

## Prompt

Add a Docker setup and one agent responsible for Docker build and deploy. When the setup is clear, build the image and create a deployable artifact.

## Decision

The deployer role is `agents/deployer.md`. `scripts/docker-build.ps1` runs `scripts/verify.ps1`, then `docker build -t sentinelflow:0.1.0` and `docker save` to `artifacts/sentinelflow-0.1.0.tar`. `scripts/docker-deploy.ps1` loads that archive and starts the Compose service. Product Java code is unchanged.

## Gate

`scripts/verify.ps1` passed on 2026-09-30T12:27:22+05:30. Tests: 21 run, 0 failures. Checkstyle: 0 violations. Secret scan: clean. Recorded in `gate.txt`.

## Failure

Docker was not installed. `winget install Docker.DockerDesktop` 4.93.0 succeeded. The installer enabled WSL 2 and Hyper-V and recorded that those features require a computer restart. After starting Docker Desktop, `docker version` reached the client (29.8.1) and the Linux engine returned HTTP 500. `wsl.exe` reports that the Windows Subsystem for Linux is not installed yet, which matches a feature that is staged but not active until reboot. `com.docker.service` stayed stopped.

## Correction

No image was built and no archive was written. Acceptance stays open until Windows has restarted and `scripts/docker-build.ps1` exits 0. The scripts now add `C:\Program Files\Docker\Docker\resources\bin` to PATH when `docker.exe` is there and the current shell has not picked it up.

## Retry after the first restart

Windows was restarted. The optional features were enabled, and virtualization-based security was already running, but the WSL app was not installed and `vmcompute.exe` was absent. The Linux engine still returned HTTP 500.

`Microsoft.WSL` 2.7.13 was installed with administrator approval. `wsl --status` then reported that WSL 2 cannot start because virtualization is not enabled, and told us to run `wsl.exe --install --no-distribution`. That command completed with: changes will not be effective until the system is rebooted. `docker info` timed out. `vmcompute.exe` is still missing. The archive was not created.

## Retry after the engine started

Docker Engine 29.8.1 answered. The first `scripts/docker-build.ps1` stopped in verify: `AlertController` called `alerts.(request, idempotencyKey)` because `ingest` had been deleted in the working copy. That call was restored to `alerts.ingest`. The second run passed verify (21 tests, 0 Checkstyle violations, secret scan clean), built `sentinelflow:0.1.0` as user `app`, and wrote `artifacts/sentinelflow-0.1.0.tar` (226192896 bytes).
