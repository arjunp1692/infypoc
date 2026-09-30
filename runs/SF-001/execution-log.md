# SF-001 execution log

## Prompt

Build the baseline triage service from `docs/requirements.md` and `docs/domain-rules.md`: ingest, normalize, replay-safe idempotency, deterministic severity, query, status, summary, correlation id, Problem Details, and a narrative that degrades when no API key is set.

## Decision

Implement narration behind `NarrativeProvider`. A blank key selects `DisabledNarrativeProvider` and never opens a socket. A configured key may return JSON, which `NarrativeValidator` accepts or rejects. Rejection stores the deterministic template.

## Failure

Spring failed to construct `NarrativeService` because the class has a production constructor and a test constructor. The context error was `No default constructor found`.

## Retry

The production constructor was marked `@Autowired`. The string constructor stayed available for unit tests.

## Failure

With Boot problem details enabled, validation responses omitted `correlationId` and the field errors. The body detail was Boot's `Invalid request content.`

## Correction

`spring.mvc.problemdetails.enabled` was set to false and `GlobalExceptionHandler` was ordered ahead of the framework handler. Validation now returns Problem Details with `correlationId` and `errors`.

## Rejected model output

`rejected-ai-output.json` asks to treat a critical malware alert as LOW and to use the action `DELETE_THE_ALERT`. `NarrativeValidator` rejects it because the severity disagrees with `SeverityRules`. `NarrativeService` then stores the CONTAIN template. The rejection is covered by `NarrativeValidatorTest` and is not sent to a public model.

## Gate

Recorded after `scripts/verify.ps1` in `gate.txt`.
