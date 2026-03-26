package com.localoffheap.egress;

import com.localoffheap.core.codec.TextEventCodec;
import com.localoffheap.core.model.StateChangeNotification;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;

public final class RealAeronNotificationPublisher implements NotificationPublisher, AutoCloseable {
    private final Publication publication;
    private final int maxOfferAttempts;

    public RealAeronNotificationPublisher(Aeron aeron, String channel, int streamId, int maxOfferAttempts) {
        this.publication = aeron.addPublication(channel, streamId);
        this.maxOfferAttempts = maxOfferAttempts;
    }

    @Override
    public void publish(StateChangeNotification notification) {
        byte[] encoded = TextEventCodec.encodeStateChangeNotification(notification);
        UnsafeBuffer buffer = new UnsafeBuffer(encoded);
        long result = Publication.BACK_PRESSURED;
        for (int i = 0; i < maxOfferAttempts; i++) {
            result = publication.offer(buffer, 0, encoded.length);
            if (result > 0) {
                return;
            }
            Thread.yield();
        }
        throw new RuntimeException("Aeron publication failed with code: " + result);
    }

    @Override
    public void close() {
        publication.close();
    }
}
