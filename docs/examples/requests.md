# Sample requests

Synthetic data only. Start the service with `powershell -File scripts/run.ps1`, then:

```
curl -s -D - http://localhost:8080/alerts -H "Content-Type: application/json" -H "X-Correlation-Id: demo-1" --data-binary @docs/examples/create-alert.json
```

Replay the same body and expect disposition `REPLAYED` with the same id.

```
curl -s "http://localhost:8080/alerts?severity=CRITICAL&source=edr"
curl -s http://localhost:8080/alerts/summary
curl -s -X PATCH http://localhost:8080/alerts/{id}/status -H "Content-Type: application/json" -d "{\"status\":\"ACKNOWLEDGED\"}"
```

Leave `SENTINELFLOW_AI_API_KEY` unset. `recommendation.source` is `DETERMINISTIC`.

A second alert for the same asset and event type inside 15 minutes returns disposition `SUPPRESSED`, the original id, `occurrenceCount` 2, and a newer `lastSeen`. Sending that second body again returns `REPLAYED` and leaves the count unchanged.
