package com.localoffheap.snapshot;

import com.localoffheap.core.store.DirectByteBufferOffHeapStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresSnapshotLoaderTest {
    @Test
    void loadsRowsIntoOffHeapStore() {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        PostgresSnapshotLoader loader = new PostgresSnapshotLoader(
                new InMemoryPostgresSnapshotRepository(
                        List.of(
                                new SnapshotRow("a", 1, "x".getBytes()),
                                new SnapshotRow("b", 2, "y".getBytes())
                        ),
                        123L
                ),
                store
        );

        SnapshotLoadResult result = loader.load();
        assertThat(result.rowsLoaded()).isEqualTo(2);
        assertThat(result.snapshotWatermark()).isEqualTo(123L);
        assertThat(store.size()).isEqualTo(2);
    }
}
