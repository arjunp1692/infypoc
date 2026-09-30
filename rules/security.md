# Security rules

- Sample data is synthetic. Hosts use names like `host-42`. Addresses use the documentation range `203.0.113.0/24` or `example.com`.
- Do not put real alerts, customer names, tokens, or the assignment email into the repository.
- Log correlation id, alert id, severity, disposition, and rule id. Do not log description, indicators, or the API key.
- Validation failures return RFC 7807 Problem Details. The body includes `correlationId` and field errors. It does not include a stack trace.
- Treat model output as data. Parse it, check the action enum, enforce the summary length, and reject any severity that disagrees with `SeverityRules`. On rejection, store the deterministic template.
- Call the model only when `sentinelflow.ai.api-key` is non-blank. Timeout is 3 seconds with one retry on 429 or 5xx.
- The idempotency key column is unique. A replay returns the original row and does not increment `occurrenceCount`.
- Secrets belong in the environment or a gitignored `.env`. Commit `.env.example` with an empty value only.
