package com.localoffheap.core.checkpoint;

import com.localoffheap.core.model.SourceType;

import java.util.Objects;

public record SourceCheckpoint(SourceType sourceType, String position) {
    public SourceCheckpoint {
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(position, "position");
    }
}
