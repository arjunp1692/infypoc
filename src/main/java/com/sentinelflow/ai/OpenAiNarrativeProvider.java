package com.sentinelflow.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.config.SentinelProperties;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class OpenAiNarrativeProvider implements NarrativeProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiNarrativeProvider.class);

    private final RestClient client;
    private final SentinelProperties.Ai ai;
    private final ObjectMapper objectMapper;

    public OpenAiNarrativeProvider(RestClient client, SentinelProperties.Ai ai, ObjectMapper objectMapper) {
        this.client = client;
        this.ai = ai;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> complete(String prompt) {
        RestClientException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return Optional.of(call(prompt));
            } catch (RestClientResponseException ex) {
                last = ex;
                int code = ex.getStatusCode().value();
                if (attempt == 2 || (code != 429 && code < 500)) {
                    break;
                }
                log.warn("ai.narrative.retry status={}", code);
            } catch (RestClientException ex) {
                last = ex;
                if (attempt == 2) {
                    break;
                }
                log.warn("ai.narrative.retry reason={}", ex.getClass().getSimpleName());
            }
        }
        log.warn("ai.narrative.unavailable reason={}", last == null ? "unknown" : last.getClass().getSimpleName());
        return Optional.empty();
    }

    private String call(String prompt) {
        String body = client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + ai.apiKey())
                .body(Map.of(
                        "model", ai.model(),
                        "temperature", 0,
                        "messages", new Object[] {
                                Map.of("role", "system", "content", "Return JSON only."),
                                Map.of("role", "user", "content", prompt)
                        }))
                .retrieve()
                .body(String.class);
        return readContent(body);
    }

    private String readContent(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception ex) {
            throw new RestClientException("model response was not readable");
        }
    }
}
