package com.localoffheap.snapshot;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryPostgresSnapshotRepository implements PostgresSnapshotRepository {
    private final List<SnapshotRow> rows;
    private final long watermark;

    public InMemoryPostgresSnapshotRepository(List<SnapshotRow> rows, long watermark) {
        this.rows = new ArrayList<>(rows);
        this.watermark = watermark;
    }

    @Override
    public List<SnapshotRow> loadSnapshotRows() {
        return List.copyOf(rows);
    }

    @Override
    public long loadSnapshotWatermark() {
        return watermark;
    }
}
