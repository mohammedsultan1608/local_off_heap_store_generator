package com.localoffheap.core.model;

import java.util.Arrays;
import java.util.Objects;

public record StateRecord(String entityKey, long version, byte[] payload) {
    public StateRecord {
        Objects.requireNonNull(entityKey, "entityKey");
        Objects.requireNonNull(payload, "payload");
        if (entityKey.isBlank()) {
            throw new IllegalArgumentException("entityKey must not be blank");
        }
        if (version <= 0) {
            throw new IllegalArgumentException("version must be > 0");
        }
    }

    @Override
    public String toString() {
        return "StateRecord{" +
                "entityKey='" + entityKey + '\'' +
                ", version=" + version +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
