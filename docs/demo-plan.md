# 10-minute demo

Start from a clean tree on Java 21. Do not set `SENTINELFLOW_AI_API_KEY`.

1. Contract (1 minute). Open `AGENTS.md`, `agents/`, and `rules/permissions.md`. Point out the loop: plan, human checkpoint, implement, test, review, record. `scripts/verify.ps1` is the only definition of done.
2. Proof-task run (2 minutes). Open `runs/SF-002/plan.md`, `checkpoint-approved.md`, and `execution-log.md`. The plan names the files that were allowed to change. The log records the rejected drop-and-replace approach and the constructor failure that was corrected.
3. Rejected model output (1 minute). Open `runs/SF-001/rejected-ai-output.json`. The payload sets severity to LOW and an action of `DELETE_THE_ALERT` for a critical malware event. `NarrativeValidator` rejects it. The stored recommendation stays the deterministic CONTAIN template. The unit test is `NarrativeValidatorTest`.
4. AI disabled (2 minutes). Run `powershell -File scripts/run.ps1`. Post `docs/examples/create-alert.json`. Show `recommendation.source` is `DETERMINISTIC` and the summary matches the critical template. Stop the process when the call returns.
5. Two guardrails (2 minutes). Show `src/main/resources/prompts/triage.txt` and say it only asks for a shape. Then show `NarrativeValidator`, the idempotency unique key, and the Checkstyle plus secret-scan steps in `scripts/verify.ps1`. Those run even if the prompt is rewritten.
6. Requirement change (2 minutes). Diff `docs/domain-rules.md` suppression section against `runs/SF-002/plan.md`, then open `SuppressionPolicyTest` and `SuppressionApiIntegrationTest`. A second event inside 15 minutes keeps the id, increments `occurrenceCount`, and moves `lastSeen`. The same body again is `REPLAYED` and does not increment. A resolved prior alert starts a new row.
