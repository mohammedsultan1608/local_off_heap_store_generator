package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

import java.util.ArrayList;
import java.util.List;

public final class KafkaNotificationPublisher implements NotificationPublisher {
    private final List<StateChangeNotification> published = new ArrayList<>();
    private volatile int failuresBeforeSuccess;

    @Override
    public synchronized void publish(StateChangeNotification notification) {
        if (failuresBeforeSuccess > 0) {
            failuresBeforeSuccess--;
            throw new RuntimeException("simulated Kafka publish failure");
        }
        published.add(notification);
    }

    public synchronized List<StateChangeNotification> published() {
        return List.copyOf(published);
    }

    public void failTimes(int failures) {
        this.failuresBeforeSuccess = failures;
    }
}
