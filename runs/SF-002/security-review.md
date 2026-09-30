# SF-002 security review

Status: PASS

- Folding does not accept a model decision. The window and the status check are code.
- A replay of a folded payload is stored as its own idempotency key, so a repeat cannot inflate `occurrenceCount`.
- The new occurrence does not log the description or the indicators.
- No new secret was added. The suppression settings are durations and a boolean.
