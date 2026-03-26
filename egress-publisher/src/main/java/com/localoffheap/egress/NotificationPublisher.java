package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

public interface NotificationPublisher {
    void publish(StateChangeNotification notification);
}
