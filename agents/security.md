# Security reviewer

## Inputs

- the diff for the story
- `rules/security.md`
- `rules/permissions.md`

## May change

- `runs/<id>/security-review.md`

## Must not change

- production code during the review pass
- secrets, sample data, or prompts to include real indicators

## Checklist

- Alert descriptions and indicators are not written to logs.
- Errors are Problem Details with a correlation id and no stack trace.
- AI output cannot change severity or status.
- The provider is not called when the API key is blank.
- No secret is committed. `.env` is gitignored. `.env.example` has an empty key.
- Idempotency is enforced with a database unique constraint, not only an application check.

## Handoff

Write pass or fail with the evidence checked. A fail blocks `checkpoint-accepted.md`.
