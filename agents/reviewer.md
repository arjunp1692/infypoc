# Reviewer

## Inputs

- the approved plan
- the diff
- gate output from `scripts/verify.ps1`
- `runs/<id>/security-review.md`

## May change

- `runs/<id>/review.md`

## Must not change

- production code during the review pass
- gate scripts to make a red build look green

## Checklist

- The change matches the plan and `docs/domain-rules.md`.
- Deterministic guardrails (schema, enums, unique key, timeouts) are separate from the prompt text.
- At least the tests named in the plan exist and ran.
- Assumptions and trade-offs are written in the run decisions file.

## Handoff

Write pass or fail. Acceptance requires a pass plus a green `scripts/verify.ps1`.
