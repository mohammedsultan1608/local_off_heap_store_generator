package com.localoffheap.ingress.aeron;

import com.localoffheap.core.codec.TextEventCodec;
import com.localoffheap.core.ingress.EventSource;
import com.localoffheap.core.model.UpsertEvent;
import io.aeron.Aeron;
import io.aeron.Subscription;

import java.util.ArrayList;
import java.util.List;

public final class RealAeronIngressAdapter implements EventSource, AutoCloseable {
    private final Subscription subscription;
    private final int fragmentLimit;

    public RealAeronIngressAdapter(Aeron aeron, String channel, int streamId, int fragmentLimit) {
        this.subscription = aeron.addSubscription(channel, streamId);
        this.fragmentLimit = fragmentLimit;
    }

    @Override
    public List<UpsertEvent> poll(int maxBatchSize) {
        List<UpsertEvent> batch = new ArrayList<>(maxBatchSize);
        subscription.poll((buffer, offset, length, header) -> {
            if (batch.size() >= maxBatchSize) {
                return;
            }
            byte[] bytes = new byte[length];
            buffer.getBytes(offset, bytes);
            batch.add(TextEventCodec.decodeUpsertEvent(bytes));
        }, Math.min(fragmentLimit, maxBatchSize));
        return batch;
    }

    @Override
    public void close() {
        subscription.close();
    }
}
