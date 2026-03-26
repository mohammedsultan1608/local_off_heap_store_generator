package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

import java.util.ArrayList;
import java.util.List;

public final class AeronNotificationPublisher implements NotificationPublisher {
    private final List<StateChangeNotification> published = new ArrayList<>();
    private volatile boolean failNext;

    @Override
    public synchronized void publish(StateChangeNotification notification) {
        if (failNext) {
            failNext = false;
            throw new RuntimeException("simulated Aeron transient failure");
        }
        published.add(notification);
    }

    public synchronized List<StateChangeNotification> published() {
        return List.copyOf(published);
    }

    public void failNextPublish() {
        this.failNext = true;
    }
}
