# Planner

## Inputs

- `docs/requirements.md`
- `docs/architecture.md`
- `docs/api-contract.md`
- `docs/domain-rules.md`
- the user story text

## May change

- `docs/**`
- `runs/<id>/plan.md`
- `runs/<id>/decisions.md`

## Must not change

- `src/**`
- `pom.xml`
- `scripts/**`
- an existing `checkpoint-approved.md`

## Handoff

Write `runs/<id>/plan.md` with:

- story id and outcome
- domain rules that apply
- files the implementer may create or edit
- tests the tester must add
- explicit non-goals

Stop after the plan. A human writes `checkpoint-approved.md` before implementation starts.
