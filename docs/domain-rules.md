# Domain rules

These rules are authoritative. Prompt text and model output do not override them.

## Normalization

- Trim `source` and `eventType`, then lowercase them.
- Trim `asset`. Case is preserved.
- Trim each indicator, drop blanks, and sort the remaining values in Unicode code-point order. Indicator case is preserved.
- Reject a payload that is missing a required field or exceeds a length limit in the API contract.

## Idempotency

An exact replay must not create a second row and must not change `occurrenceCount`.

- If `Idempotency-Key` is present, that trimmed value is the key.
- Otherwise the key is the hex SHA-256 of `source|eventType|asset|timestamp|indicator1,indicator2,...` using the normalized fields and a comma-joined indicator list.
- The key is unique in the database.
- A repeat returns the stored alert with disposition `REPLAYED`.

Idempotency is not suppression. A new timestamp or a new indicator set is a different event.

## Severity

First match wins.

1. Event type `ransomware`, `malware`, `data_exfiltration`, or `privilege_escalation` is CRITICAL with rule id `SEV-CRITICAL-TYPE`.
2. Event type `brute_force`, `suspicious_login`, or `policy_violation` is HIGH with rule id `SEV-HIGH-TYPE`.
3. Three or more indicators is HIGH with rule id `SEV-HIGH-INDICATORS`.
4. Event type `informational` is LOW with rule id `SEV-LOW-INFORMATIONAL`.
5. Any other event type is MEDIUM with rule id `SEV-MEDIUM-DEFAULT`.

An `informational` event with three or more indicators is HIGH because rule 3 matches before rule 4.

## Status

New alerts start at `OPEN`. Allowed values are `OPEN`, `ACKNOWLEDGED`, `IN_PROGRESS`, `RESOLVED`, and `FALSE_POSITIVE`.

## Recommendation

The deterministic template is always available.

- CRITICAL uses action `CONTAIN` and summary `Critical security event on {asset}: {eventType}. Immediate containment required.`
- HIGH uses action `INVESTIGATE` and summary `High severity event on {asset}: {eventType}. Investigate promptly.`
- MEDIUM uses action `MONITOR` and summary `Medium severity event on {asset}: {eventType}. Monitor and investigate.`
- LOW uses action `MONITOR` and summary `Informational event on {asset}: {eventType}.`

When the API key is blank, the provider is not called and `recommendation.source` is `DETERMINISTIC`.

When the key is set, the provider may return JSON with `summary` (1 to 400 characters) and `recommendedAction` (`INVESTIGATE`, `CONTAIN`, `MONITOR`, or `ESCALATE`).

`NarrativeValidator` rejects the payload when JSON parsing fails, the summary is blank or longer than 400 characters, the action is outside the enum, or a `severity` field is present and differs from the deterministic severity. Rejection keeps the template. The raw rejection is recorded without the alert description.

The prompt may mention this shape. The validator is the guardrail that runs.

## Suppression

A later alert with the same normalized asset and event type folds into an existing row when suppression is enabled and all of the following hold:

- the idempotency key has not been seen before
- the existing status is not `RESOLVED` or `FALSE_POSITIVE`
- the new timestamp is greater than or equal to `lastSeen`
- the new timestamp is less than or equal to `lastSeen` plus `sentinelflow.suppression.window`

The default window is 15 minutes. The row keeps its id. `occurrenceCount` increases by one. `lastSeen` becomes the new timestamp. The response disposition is `SUPPRESSED`.

An exact replay of any accepted payload, including one that was folded, returns `REPLAYED` and does not increase the count. Each accepted payload stores its idempotency key. A new event outside the window, or a new event after the prior row was resolved, creates a new row.

Dropping the repeated alert, or replacing the row so the earlier occurrence disappears, is rejected. The id, the count, and the previous first-seen time stay.
