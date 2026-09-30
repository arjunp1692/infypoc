# SF-002 plan — configurable suppression

Status: approved as a separate story after SF-001. See `checkpoint-approved.md`.

## Outcome

Add configurable suppression of a later alert that shares a normalized asset and event type. Keep the existing id. Increment `occurrenceCount`. Set `lastSeen` to the new event timestamp. Return disposition `SUPPRESSED`.

An exact idempotent replay still returns `REPLAYED` and does not increment the count. A later alert outside the window, or a later alert after the prior row is `RESOLVED` or `FALSE_POSITIVE`, creates a new row.

## Configuration

- `sentinelflow.suppression.enabled` default `true`
- `sentinelflow.suppression.window` default `PT15M`

## Rule

Fold when all of the following hold:

- suppression is enabled
- the idempotency key was not seen before
- an existing row has the same normalized asset and event type
- that row's status is not `RESOLVED` or `FALSE_POSITIVE`
- the new timestamp is greater than or equal to `lastSeen`
- the new timestamp is less than or equal to `lastSeen` plus the window

If several rows match, fold into the one with the latest `lastSeen`.

## Context and tests that must change

- `docs/domain-rules.md` gains the suppression section as an active rule
- `docs/api-contract.md` documents disposition `SUPPRESSED`
- `docs/requirements.md` marks SF-002 as delivered behavior
- Unit test for the window, a resolved prior alert, and an exact replay
- Integration test that a second event inside the window increments the count and moves `lastSeen`

## Files the implementer may change

- `src/main/java/com/sentinelflow/triage/SuppressionPolicy.java` (new)
- `src/main/java/com/sentinelflow/service/AlertService.java`
- `src/main/java/com/sentinelflow/persistence/AlertRepository.java`
- `src/main/resources/application.yml`
- the docs listed above
- the tests listed above

## Rejected approach

Dropping the repeated alert, or replacing the row so the earlier occurrence disappears, fails this story. The count and the previous id have to remain.

## Non-goals

- Cross-instance locking.
- Changing severity because more occurrences arrived.
