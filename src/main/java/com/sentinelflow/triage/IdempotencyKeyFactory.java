package com.sentinelflow.triage;

import com.sentinelflow.domain.NormalizedAlert;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyKeyFactory {

    public String create(String header, NormalizedAlert alert) {
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        String material = String.join("|",
                alert.source(),
                alert.eventType(),
                alert.asset(),
                alert.timestamp().toString(),
                String.join(",", alert.indicators()));
        return sha256(material);
    }

    private static String sha256(String material) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(material.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required", ex);
        }
    }
}
