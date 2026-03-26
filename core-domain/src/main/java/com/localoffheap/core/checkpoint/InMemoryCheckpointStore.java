package com.localoffheap.core.checkpoint;

import com.localoffheap.core.model.SourceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryCheckpointStore implements CheckpointStore {
    private final Map<SourceType, SourceCheckpoint> state = new EnumMap<>(SourceType.class);

    @Override
    public synchronized Optional<SourceCheckpoint> get(SourceType sourceType) {
        return Optional.ofNullable(state.get(sourceType));
    }

    @Override
    public synchronized void save(SourceCheckpoint checkpoint) {
        state.put(checkpoint.sourceType(), checkpoint);
    }
}
