# SF-002 decisions

- Suppression is not idempotency. A new timestamp or indicator set is a new event and may fold. An exact replay must not increment `occurrenceCount`.
- Each accepted payload stores its key in `idempotency_key`, including keys for folded events. Replay lookup checks the original column and that table.
- The window is `sentinelflow.suppression.window`, default `PT15M`, and can be turned off with `sentinelflow.suppression.enabled`.
- Folding keeps the original description and indicators. Only the count and `lastSeen` change.
- A suggestion to delete the repeated alert, or to replace the row so the first occurrence disappears, was rejected. The story requires the same id, the count, and the previous first-seen time.
