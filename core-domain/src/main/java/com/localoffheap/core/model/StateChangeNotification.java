package com.localoffheap.core.model;

import java.util.Objects;

public record StateChangeNotification(
        String entityKey,
        long oldVersion,
        long newVersion,
        String changeType,
        long appliedAtEpochMs,
        byte[] payload) {

    public StateChangeNotification {
        Objects.requireNonNull(entityKey, "entityKey");
        Objects.requireNonNull(changeType, "changeType");
        Objects.requireNonNull(payload, "payload");
    }
}
