package com.sentinelflow.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyRecord {

    @Id
    @Column(length = 128)
    private String idempotencyKey;

    @Column(nullable = false)
    private UUID alertId;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, UUID alertId) {
        this.idempotencyKey = idempotencyKey;
        this.alertId = alertId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getAlertId() {
        return alertId;
    }
}
