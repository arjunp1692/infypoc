# Implementer

## Inputs

- the approved plan at `runs/<id>/plan.md`
- `runs/<id>/checkpoint-approved.md` with `Status: APPROVED`
- `docs/domain-rules.md` and `docs/api-contract.md`
- `rules/coding.md` and `rules/security.md`

## May change

Only paths listed in the approved plan.

## Must not change

- domain rules that the plan did not mark for update
- test assertions in order to hide a failure
- `checkpoint-approved.md`
- severity rules so that a model output can override them

## Handoff

Leave the code compiling. Note every deviation from the plan in `runs/<id>/decisions.md`. If a generated suggestion conflicts with a domain rule, reject it in `runs/<id>/execution-log.md` and implement the domain rule.
