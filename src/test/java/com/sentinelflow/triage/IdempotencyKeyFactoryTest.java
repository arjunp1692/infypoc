package com.sentinelflow.triage;

import static org.assertj.core.api.Assertions.assertThat;

import com.sentinelflow.domain.NormalizedAlert;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class IdempotencyKeyFactoryTest {

    private final IdempotencyKeyFactory keys = new IdempotencyKeyFactory();
    private final Instant timestamp = Instant.parse("2026-09-29T12:00:00Z");

    @Test
    void headerKeyWinsAndIsTrimmed() {
        NormalizedAlert alert = sample(List.of("ip:203.0.113.10"));
        assertThat(keys.create("  client-key  ", alert)).isEqualTo("client-key");
    }

    @Test
    void derivedKeyIsStableForTheSameNormalizedFields() {
        NormalizedAlert alert = sample(List.of("host:workstation.example.com", "ip:203.0.113.10"));
        assertThat(keys.create(null, alert)).isEqualTo(keys.create("   ", alert));
    }

    @Test
    void derivedKeyChangesWhenTheTimestampChanges() {
        NormalizedAlert first = sample(List.of("ip:203.0.113.10"));
        NormalizedAlert later = new NormalizedAlert(
                first.source(),
                first.eventType(),
                first.asset(),
                timestamp.plusSeconds(60),
                first.description(),
                first.indicators());
        assertThat(keys.create(null, first)).isNotEqualTo(keys.create(null, later));
    }

    private NormalizedAlert sample(List<String> indicators) {
        return new NormalizedAlert("edr", "malware", "host-42", timestamp, "lab event", indicators);
    }
}
