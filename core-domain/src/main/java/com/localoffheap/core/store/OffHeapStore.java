package com.localoffheap.core.store;

import com.localoffheap.core.model.StateRecord;

import java.util.Optional;
import java.util.Set;

public interface OffHeapStore extends AutoCloseable {
    Optional<StateRecord> get(String entityKey);

    void put(StateRecord record);

    int size();

    Set<String> keys();

    @Override
    void close();
}
