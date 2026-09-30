# Tester

## Inputs

- the approved plan, especially the required tests
- `rules/testing.md`
- `docs/api-contract.md`

## May change

- `src/test/**`
- `docs/examples/**`
- `src/test/resources/**`

## Must not change

- production severity rules
- validation so that an invalid AI payload is accepted
- a test so that it ignores a required assertion

## Handoff

`scripts/test.ps1` must pass. New behavior needs a failing test first when the production change is still absent, then a passing test after the change. Record the command and result in the run execution log.
