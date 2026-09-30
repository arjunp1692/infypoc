# SF-001 security review

Status: PASS

- Descriptions and indicators are not log arguments. Ingest logs id, severity, and rule id.
- Validation and not-found responses are Problem Details with a correlation id and no stack trace. Unexpected failures log the correlation id and return a fixed detail.
- `NarrativeValidator` rejects a severity that disagrees with the deterministic decision and rejects actions outside the allow list.
- The test profile sets an empty API key. `NarrativeValidatorTest.serviceDoesNotCallTheProviderWithoutAKey` fails if the provider is invoked.
- `.env` is gitignored. `.env.example` has an empty key. `scripts/secret-scan.ps1` is part of verify.
- The idempotency key is unique in the database, not only in the service check.
