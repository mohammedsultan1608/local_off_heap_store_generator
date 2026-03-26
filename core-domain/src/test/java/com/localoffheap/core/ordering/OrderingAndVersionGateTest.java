package com.localoffheap.core.ordering;

import com.localoffheap.core.apply.ApplyStatus;
import com.localoffheap.core.model.OperationType;
import com.localoffheap.core.model.SourceType;
import com.localoffheap.core.model.StateRecord;
import com.localoffheap.core.model.UpsertEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderingAndVersionGateTest {
    private final OrderingAndVersionGate gate = new OrderingAndVersionGate();

    @Test
    void acceptsStrictlyHigherVersion() {
        ApplyStatus status = gate.evaluate(
                Optional.of(new StateRecord("k", 1, "a".getBytes())),
                new UpsertEvent("k", 2, OperationType.UPSERT, "b".getBytes(), 1L, SourceType.KAFKA, "t-1")
        );
        assertThat(status).isEqualTo(ApplyStatus.APPLIED);
    }

    @Test
    void dropsDuplicateSameVersionAndPayload() {
        ApplyStatus status = gate.evaluate(
                Optional.of(new StateRecord("k", 2, "a".getBytes())),
                new UpsertEvent("k", 2, OperationType.UPSERT, "a".getBytes(), 1L, SourceType.AERON, "a-1")
        );
        assertThat(status).isEqualTo(ApplyStatus.DUPLICATE);
    }

    @Test
    void rejectsStale() {
        ApplyStatus status = gate.evaluate(
                Optional.of(new StateRecord("k", 3, "a".getBytes())),
                new UpsertEvent("k", 2, OperationType.UPSERT, "b".getBytes(), 1L, SourceType.KAFKA, "t-2")
        );
        assertThat(status).isEqualTo(ApplyStatus.STALE);
    }
}
