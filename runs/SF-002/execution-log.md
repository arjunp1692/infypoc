# SF-002 execution log

## Prompt

Add configurable suppression of repeated alerts from the same asset and event type. Retain occurrence count and last-seen time. Evolve `docs/domain-rules.md`, the API contract, and the tests in the same change.

## Decision

`SuppressionPolicy` is a pure function. `AlertService` calls it only after the idempotency lookup misses. A match that is not resolved and is still inside the window updates that row and returns disposition `SUPPRESSED`.

## Rejected suggestion

Drop the repeated alert, or replace the stored row so the earlier occurrence is gone. That fails the story: the id, `occurrenceCount`, and `firstSeen` have to remain. The implementation folds into the existing row instead. This rejection is also written in `runs/SF-002/plan.md` and `docs/domain-rules.md`.

## Failure

`SentinelProperties` grew a second constructor so older tests could omit the suppression settings. Spring then refused to bind the record (`No default constructor found`) and the application context did not start.

## Correction

The extra constructor was removed. The record has one canonical constructor. Unit tests pass both the AI settings and the suppression settings. `application.yml` and `application-test.yml` set `enabled: true` and `window: PT15M`.

## Failure

The first suppression integration test did not compile. `mockMvc.perform` was given a `ResultActions` value.

## Correction

The summary call uses `get("/alerts/summary")` directly. `SuppressionApiIntegrationTest` then asserts the folded count, a replay that does not increment, a resolved prior alert, and an event outside the window.

## Gate

Recorded after `scripts/verify.ps1` in `gate.txt`.
