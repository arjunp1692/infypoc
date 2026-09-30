# SentinelFlow agent contract

This file is the persistent project contract. Product behavior changes only through an approved run under `runs/`. Chat history is not a source of truth. Curated context lives in `docs/`.

## Mission

Deliver SentinelFlow, a local security-event triage service, through a delivery loop a reviewer can replay:

understand → plan → approve → implement → test → review → record

The service must stay usable when no AI provider is configured. Severity is always deterministic. A model may only propose a summary and a recommended action, and that output is untrusted until `NarrativeValidator` accepts it.

## Story loop

1. Read `docs/requirements.md`, `docs/architecture.md`, `docs/api-contract.md`, and `docs/domain-rules.md`.
2. Write `runs/<id>/plan.md` before touching `src/`. The plan names the story, the files that may change, the tests that must exist, and the decisions that are out of scope.
3. Stop for a human checkpoint. Coding starts only after `runs/<id>/checkpoint-approved.md` contains `Status: APPROVED`.
4. Implement only the files named in the plan. Follow `rules/` and the role file in `agents/` for the current step.
5. Run `scripts/verify.ps1`. A failing gate blocks acceptance.
6. Security and review roles write their notes into the run folder.
7. Record prompts, decisions, failures, retries, and corrections in `runs/<id>/execution-log.md`.
8. Write `runs/<id>/checkpoint-accepted.md` only after verify is green.

## Roles

| Role | File | May change |
| --- | --- | --- |
| Planner | `agents/planner.md` | `docs/`, `runs/` |
| Implementer | `agents/implementer.md` | files named in the approved plan |
| Tester | `agents/tester.md` | `src/test/`, `docs/examples/` |
| Security | `agents/security.md` | review notes in `runs/` only |
| Reviewer | `agents/reviewer.md` | review notes in `runs/` only |
| Deployer | `agents/deployer.md` | `Dockerfile`, Compose, `.dockerignore`, `scripts/docker-*.ps1` |

## Deterministic gates

`scripts/verify.ps1` is the definition of done:

- `scripts/build.ps1` — compile and package
- `scripts/test.ps1` — unit and integration tests
- `scripts/lint.ps1` — Checkstyle
- `scripts/secret-scan.ps1` — reject private keys and live credentials

Prompt text is not a gate. Schema checks, enums, timeouts, and the secret scan are gates.

## Command permissions

Allow and deny lists are in `rules/permissions.md`. Do not force-push, do not commit `.env` or key material, and do not send alert payloads to a public model.

## Current stories

- `runs/SF-001` — baseline ingest, normalization, idempotent replay, deterministic severity, query, status, summary, and optional narration with a degraded path.
- `runs/SF-002` — configurable suppression of repeated alerts for the same asset and event type, retaining occurrence count and last-seen time.
- `runs/SF-003` — container build. The deployer role runs verify, then writes `artifacts/sentinelflow-0.1.0.tar`.
