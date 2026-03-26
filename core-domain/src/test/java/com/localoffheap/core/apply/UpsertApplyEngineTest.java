package com.localoffheap.core.apply;

import com.localoffheap.core.model.OperationType;
import com.localoffheap.core.model.SourceType;
import com.localoffheap.core.ordering.OrderingAndVersionGate;
import com.localoffheap.core.store.DirectByteBufferOffHeapStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class UpsertApplyEngineTest {
    @Test
    void appliesAndEmitsNotification() {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        UpsertApplyEngine engine = new UpsertApplyEngine(store, new OrderingAndVersionGate(), Clock.systemUTC());

        ApplyResult first = engine.apply(new com.localoffheap.core.model.UpsertEvent(
                "e1", 1, OperationType.UPSERT, "v1".getBytes(), 1L, SourceType.KAFKA, "k-1"
        ));
        ApplyResult duplicate = engine.apply(new com.localoffheap.core.model.UpsertEvent(
                "e1", 1, OperationType.UPSERT, "v1".getBytes(), 1L, SourceType.KAFKA, "k-1"
        ));

        assertThat(first.status()).isEqualTo(ApplyStatus.APPLIED);
        assertThat(first.notification()).isPresent();
        assertThat(duplicate.status()).isEqualTo(ApplyStatus.DUPLICATE);
        assertThat(store.get("e1")).isPresent();
        assertThat(store.get("e1").orElseThrow().version()).isEqualTo(1);
    }
}
