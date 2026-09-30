# Coding rules

- Java 21 and Spring Boot 3.5. Package root is `com.sentinelflow`.
- Constructor injection. No field injection.
- Domain decisions live in plain classes (`SeverityRules`, `IdempotencyKeyFactory`, `SuppressionPolicy`, `NarrativeValidator`) so they can be unit tested without the web tier.
- Controllers translate HTTP. They do not assign severity.
- Persist with Spring Data JPA. The default database is file-based H2 at `./data/sentinelflow`.
- Public API paths are exactly `POST /alerts`, `GET /alerts`, `GET /alerts/{id}`, `PATCH /alerts/{id}/status`, and `GET /alerts/summary`.
- Request and response JSON uses camelCase.
- Time values are UTC instants in ISO-8601.
- Checkstyle must pass. No tab characters. No unused imports.
- Do not add a UI in this assignment.
