package com.localoffheap.core.apply;

import com.localoffheap.core.model.StateChangeNotification;
import com.localoffheap.core.model.StateRecord;
import com.localoffheap.core.model.UpsertEvent;
import com.localoffheap.core.ordering.OrderingAndVersionGate;
import com.localoffheap.core.store.OffHeapStore;

import java.time.Clock;
import java.util.Optional;

public final class UpsertApplyEngine {
    private final OffHeapStore store;
    private final OrderingAndVersionGate gate;
    private final Clock clock;

    public UpsertApplyEngine(OffHeapStore store, OrderingAndVersionGate gate, Clock clock) {
        this.store = store;
        this.gate = gate;
        this.clock = clock;
    }

    public ApplyResult apply(UpsertEvent event) {
        Optional<StateRecord> current = store.get(event.entityKey());
        ApplyStatus status = gate.evaluate(current, event);
        if (status != ApplyStatus.APPLIED) {
            return new ApplyResult(status, Optional.empty());
        }

        long oldVersion = current.map(StateRecord::version).orElse(0L);
        store.put(new StateRecord(event.entityKey(), event.version(), event.payload()));
        StateChangeNotification notification = new StateChangeNotification(
                event.entityKey(),
                oldVersion,
                event.version(),
                "UPSERT_APPLIED",
                clock.millis(),
                event.payload()
        );
        return new ApplyResult(ApplyStatus.APPLIED, Optional.of(notification));
    }
}
