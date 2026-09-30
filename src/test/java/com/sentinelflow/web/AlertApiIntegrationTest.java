package com.sentinelflow.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sentinelflow.persistence.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alerts;

    @BeforeEach
    void clean() {
        alerts.deleteAll();
    }

    @Test
    void createThenFetchAndSummarize() throws Exception {
        String created = mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "corr-create")
                        .content(body("2026-09-29T12:00:00Z", "ip:203.0.113.10", "")))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "corr-create"))
                .andExpect(jsonPath("$.source").value("edr"))
                .andExpect(jsonPath("$.eventType").value("malware"))
                .andExpect(jsonPath("$.asset").value("host-42"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.ruleId").value("SEV-CRITICAL-TYPE"))
                .andExpect(jsonPath("$.occurrenceCount").value(1))
                .andExpect(jsonPath("$.disposition").value("CREATED"))
                .andExpect(jsonPath("$.recommendation.source").value("DETERMINISTIC"))
                .andExpect(jsonPath("$.recommendation.action").value("CONTAIN"))
                .andExpect(jsonPath("$.indicators[0]").value("host:workstation.example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/alerts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.disposition").value("FETCHED"));

        mockMvc.perform(get("/alerts")
                        .param("severity", "CRITICAL")
                        .param("source", "EDR")
                        .param("from", "2026-09-29T00:00:00Z")
                        .param("to", "2026-09-29T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/alerts/summary").param("severity", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.bySeverity.CRITICAL").value(1))
                .andExpect(jsonPath("$.suppressedOccurrences").value(0));

        mockMvc.perform(patch("/alerts/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.disposition").value("UPDATED"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void replayDoesNotDuplicate() throws Exception {
        String payload = body("2026-09-29T12:00:00Z", "ip:203.0.113.10");
        String created = mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.disposition").value("REPLAYED"))
                .andExpect(jsonPath("$.occurrenceCount").value(1));

        assertThat(alerts.count()).isEqualTo(1);
    }

    @Test
    void missingApiKeyStillTriages() throws Exception {
        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("2026-09-29T12:05:00Z", "ip:203.0.113.20")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recommendation.source").value("DETERMINISTIC"))
                .andExpect(jsonPath("$.recommendation.summary").value(
                        "Critical security event on host-42: malware. Immediate containment required."));
    }

    @Test
    void validationErrorCarriesCorrelationId() throws Exception {
        mockMvc.perform(post("/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "corr-bad")
                        .content("{\"eventType\":\"malware\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-bad"))
                .andExpect(jsonPath("$.correlationId").value("corr-bad"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    private static String body(String timestamp, String... indicators) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < indicators.length; i++) {
            if (i > 0) {
                joined.append(',');
            }
            joined.append('"').append(indicators[i]).append('"');
        }
        return """
                {
                  "source": " EDR ",
                  "eventType": "Malware",
                  "timestamp": "%s",
                  "asset": " host-42 ",
                  "description": "Suspicious process started",
                  "indicators": [%s, "host:workstation.example.com"]
                }
                """.formatted(timestamp, joined);
    }
}
