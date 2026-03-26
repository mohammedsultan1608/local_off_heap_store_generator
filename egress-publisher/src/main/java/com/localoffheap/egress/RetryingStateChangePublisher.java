package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

public final class RetryingStateChangePublisher {
    private final NotificationPublisher aeronPublisher;
    private final NotificationPublisher kafkaPublisher;
    private final DeadLetterPublisher deadLetterPublisher;
    private final int maxRetries;

    public RetryingStateChangePublisher(
            NotificationPublisher aeronPublisher,
            NotificationPublisher kafkaPublisher,
            DeadLetterPublisher deadLetterPublisher,
            int maxRetries) {
        this.aeronPublisher = aeronPublisher;
        this.kafkaPublisher = kafkaPublisher;
        this.deadLetterPublisher = deadLetterPublisher;
        this.maxRetries = maxRetries;
    }

    public void publish(StateChangeNotification notification) {
        publishWithRetry(aeronPublisher, notification, false);
        publishWithRetry(kafkaPublisher, notification, true);
    }

    private void publishWithRetry(
            NotificationPublisher publisher,
            StateChangeNotification notification,
            boolean sendToDlqOnFailure) {
        RuntimeException lastFailure = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                publisher.publish(notification);
                return;
            } catch (RuntimeException ex) {
                lastFailure = ex;
            }
        }

        if (sendToDlqOnFailure) {
            deadLetterPublisher.publish(notification, lastFailure);
            return;
        }
        throw lastFailure;
    }
}
