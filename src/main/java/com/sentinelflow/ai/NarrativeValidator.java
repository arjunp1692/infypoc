package com.sentinelflow.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.domain.Narrative;
import com.sentinelflow.domain.NarrativeSource;
import com.sentinelflow.domain.RecommendedAction;
import com.sentinelflow.domain.Severity;

public class NarrativeValidator {

    private final ObjectMapper objectMapper;
    private final int maxSummaryLength;

    public NarrativeValidator(ObjectMapper objectMapper, int maxSummaryLength) {
        this.objectMapper = objectMapper;
        this.maxSummaryLength = maxSummaryLength;
    }

    public ValidationResult validate(String raw, Severity authoritative) {
        if (raw == null || raw.isBlank()) {
            return ValidationResult.reject("empty model output");
        }
        JsonNode node;
        try {
            node = objectMapper.readTree(raw);
        } catch (Exception ex) {
            return ValidationResult.reject("model output is not json");
        }
        if (node == null || !node.isObject()) {
            return ValidationResult.reject("model output is not an object");
        }
        if (node.has("severity")) {
            String claimed = node.get("severity").asText("");
            if (!authoritative.name().equals(claimed)) {
                return ValidationResult.reject("model severity disagrees with deterministic severity");
            }
        }
        if (!node.hasNonNull("recommendedAction") || !node.hasNonNull("summary")) {
            return ValidationResult.reject("summary or recommendedAction is missing");
        }
        String summary = node.get("summary").asText("");
        if (summary.isBlank() || summary.length() > maxSummaryLength) {
            return ValidationResult.reject("summary length is outside 1.." + maxSummaryLength);
        }
        RecommendedAction action;
        try {
            action = RecommendedAction.valueOf(node.get("recommendedAction").asText(""));
        } catch (IllegalArgumentException ex) {
            return ValidationResult.reject("recommendedAction is not allowed");
        }
        return ValidationResult.accept(new Narrative(NarrativeSource.AI, summary, action));
    }

    public record ValidationResult(boolean accepted, Narrative narrative, String reason) {

        public static ValidationResult accept(Narrative narrative) {
            return new ValidationResult(true, narrative, "");
        }

        public static ValidationResult reject(String reason) {
            return new ValidationResult(false, null, reason);
        }
    }
}
