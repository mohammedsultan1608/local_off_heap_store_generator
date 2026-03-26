package com.localoffheap.snapshot;

public record SnapshotLoadResult(long snapshotWatermark, int rowsLoaded) {
}
