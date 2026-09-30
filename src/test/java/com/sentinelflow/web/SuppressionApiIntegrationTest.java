package com.sentinelflow.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sentinelflow.persistence.AlertRepository;
import com.sentinelflow.persistence.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SuppressionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alerts;

    @Autowired
    private IdempotencyRepository idempotencyKeys;

    @BeforeEach
    void clean() {
        idempotencyKeys.deleteAll();
        alerts.deleteAll();
    }

    @Test
    void repeatedAlertKeepsCountAndLastSeen() throws Exception {
        String id = create("2026-09-29T12:00:00Z", "ip:203.0.113.10");

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("2026-09-29T12:10:00Z", "ip:203.0.113.11")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.disposition").value("SUPPRESSED"))
                .andExpect(jsonPath("$.occurrenceCount").value(2))
                .andExpect(jsonPath("$.firstSeen").value("2026-09-29T12:00:00Z"))
                .andExpect(jsonPath("$.lastSeen").value("2026-09-29T12:10:00Z"));
        assertThat(alerts.count()).isEqualTo(1);

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("2026-09-29T12:10:00Z", "ip:203.0.113.11")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disposition").value("REPLAYED"))
                .andExpect(jsonPath("$.occurrenceCount").value(2));

        mockMvc.perform(get("/alerts/summary"))
                .andExpect(jsonPath("$.suppressedOccurrences").value(1));
    }

    @Test
    void resolvedOrExpiredAlertStartsANewRow() throws Exception {
        String id = create("2026-09-29T12:00:00Z", "ip:203.0.113.20");
        mockMvc.perform(patch("/alerts/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("2026-09-29T12:05:00Z", "ip:203.0.113.21")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.disposition").value("CREATED"))
                .andExpect(jsonPath("$.occurrenceCount").value(1));

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("2026-09-29T13:00:00Z", "ip:203.0.113.22")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occurrenceCount").value(1));
        assertThat(alerts.count()).isEqualTo(3);
    }

    private String create(String timestamp, String indicator) throws Exception {
        MvcResult result = mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(timestamp, indicator)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private static String payload(String timestamp, String indicator) {
        return """
                {
                  "source": "edr",
                  "eventType": "malware",
                  "timestamp": "%s",
                  "asset": "host-42",
                  "description": "Suspicious process started",
                  "indicators": ["%s"]
                }
                """.formatted(timestamp, indicator);
    }
}
