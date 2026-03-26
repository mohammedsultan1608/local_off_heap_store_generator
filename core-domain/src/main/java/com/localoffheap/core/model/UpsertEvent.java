package com.localoffheap.core.model;

import java.util.Arrays;
import java.util.Objects;

public record UpsertEvent(
        String entityKey,
        long version,
        OperationType operationType,
        byte[] payload,
        long eventTimeEpochMs,
        SourceType sourceType,
        String sourcePosition) {

    public UpsertEvent {
        Objects.requireNonNull(entityKey, "entityKey");
        Objects.requireNonNull(operationType, "operationType");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(sourcePosition, "sourcePosition");
        if (entityKey.isBlank()) {
            throw new IllegalArgumentException("entityKey must not be blank");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("version must be > 0");
        }
    }

    @Override
    public String toString() {
        return "UpsertEvent{" +
                "entityKey='" + entityKey + '\'' +
                ", version=" + version +
                ", operationType=" + operationType +
                ", payload=" + Arrays.toString(payload) +
                ", sourceType=" + sourceType +
                ", sourcePosition='" + sourcePosition + '\'' +
                '}';
    }
}
