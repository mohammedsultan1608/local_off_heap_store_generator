package com.localoffheap.core.checkpoint;

import com.localoffheap.core.model.SourceType;

import java.util.Optional;

public interface CheckpointStore {
    Optional<SourceCheckpoint> get(SourceType sourceType);

    void save(SourceCheckpoint checkpoint);
}
