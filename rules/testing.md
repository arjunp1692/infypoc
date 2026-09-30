# Testing rules

- JUnit 5 and AssertJ. Web tests use MockMvc and the `test` profile (in-memory H2).
- Minimum bar: 3 unit tests and 2 integration tests. This project exceeds that bar.
- Unit tests cover severity rules, idempotency key derivation, AI output rejection, and the suppression window.
- Integration tests cover create-then-fetch, replay without duplication, triage while the AI key is absent, and suppression of a repeated asset and event type.
- Tests must not call a live model.
- A rejected model payload lives in `src/test/resources/ai/rejected-narrative.json` and is asserted, not hand-waved.
- Do not disable a test to turn the gate green.
