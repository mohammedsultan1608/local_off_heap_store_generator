package com.localoffheap.core.ordering;

import com.localoffheap.core.apply.ApplyStatus;
import com.localoffheap.core.model.StateRecord;
import com.localoffheap.core.model.UpsertEvent;

import java.util.Arrays;
import java.util.Optional;

public final class OrderingAndVersionGate {

    public ApplyStatus evaluate(Optional<StateRecord> current, UpsertEvent incoming) {
        if (current.isEmpty()) {
            return ApplyStatus.APPLIED;
        }

        StateRecord existing = current.get();
        if (incoming.version() > existing.version()) {
            return ApplyStatus.APPLIED;
        }

        if (incoming.version() == existing.version() &&
                Arrays.equals(incoming.payload(), existing.payload())) {
            return ApplyStatus.DUPLICATE;
        }

        return ApplyStatus.STALE;
    }
}
