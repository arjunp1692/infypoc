package com.sentinelflow.web;

import java.util.UUID;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(UUID id) {
        super(id.toString());
    }
}
