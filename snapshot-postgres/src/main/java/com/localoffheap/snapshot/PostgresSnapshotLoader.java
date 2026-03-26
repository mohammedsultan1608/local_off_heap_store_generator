package com.localoffheap.snapshot;

import com.localoffheap.core.model.StateRecord;
import com.localoffheap.core.store.OffHeapStore;

import java.util.List;

public final class PostgresSnapshotLoader {
    private final PostgresSnapshotRepository repository;
    private final OffHeapStore store;

    public PostgresSnapshotLoader(PostgresSnapshotRepository repository, OffHeapStore store) {
        this.repository = repository;
        this.store = store;
    }

    public SnapshotLoadResult load() {
        List<SnapshotRow> rows = repository.loadSnapshotRows();
        for (SnapshotRow row : rows) {
            store.put(new StateRecord(row.entityKey(), row.version(), row.payload()));
        }
        long watermark = repository.loadSnapshotWatermark();
        return new SnapshotLoadResult(watermark, rows.size());
    }
}
