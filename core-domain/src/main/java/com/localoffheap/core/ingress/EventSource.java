package com.localoffheap.core.ingress;

import com.localoffheap.core.model.UpsertEvent;

import java.util.List;

public interface EventSource {
    List<UpsertEvent> poll(int maxBatchSize);
}
