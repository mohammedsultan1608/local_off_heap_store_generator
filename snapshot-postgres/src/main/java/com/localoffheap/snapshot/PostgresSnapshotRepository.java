package com.localoffheap.snapshot;

import java.util.List;

public interface PostgresSnapshotRepository {
    List<SnapshotRow> loadSnapshotRows();

    long loadSnapshotWatermark();
}
