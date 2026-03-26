package com.localoffheap.ingress.aeron;

import com.localoffheap.core.ingress.EventSource;
import com.localoffheap.core.model.UpsertEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AeronIngressAdapter implements EventSource {
    private final Queue<UpsertEvent> queue = new ConcurrentLinkedQueue<>();

    public void accept(UpsertEvent event) {
        queue.add(event);
    }

    @Override
    public List<UpsertEvent> poll(int maxBatchSize) {
        List<UpsertEvent> batch = new ArrayList<>(maxBatchSize);
        for (int i = 0; i < maxBatchSize; i++) {
            UpsertEvent event = queue.poll();
            if (event == null) {
                break;
            }
            batch.add(event);
        }
        return batch;
    }
}
