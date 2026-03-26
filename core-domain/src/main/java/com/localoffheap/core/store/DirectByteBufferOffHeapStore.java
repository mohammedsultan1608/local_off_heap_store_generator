package com.localoffheap.core.store;

import com.localoffheap.core.model.StateRecord;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DirectByteBufferOffHeapStore implements OffHeapStore {
    private final Map<String, Entry> index = new ConcurrentHashMap<>();

    @Override
    public Optional<StateRecord> get(String entityKey) {
        Entry entry = index.get(entityKey);
        if (entry == null) {
            return Optional.empty();
        }
        byte[] payload = new byte[entry.buffer.capacity()];
        ByteBuffer duplicate = entry.buffer.asReadOnlyBuffer();
        duplicate.position(0);
        duplicate.get(payload);
        return Optional.of(new StateRecord(entityKey, entry.version, payload));
    }

    @Override
    public void put(StateRecord record) {
        ByteBuffer direct = ByteBuffer.allocateDirect(record.payload().length);
        direct.put(record.payload());
        direct.flip();
        index.put(record.entityKey(), new Entry(record.version(), direct));
    }

    @Override
    public int size() {
        return index.size();
    }

    @Override
    public Set<String> keys() {
        return index.keySet();
    }

    @Override
    public void close() {
        index.clear();
    }

    private record Entry(long version, ByteBuffer buffer) {
    }
}
