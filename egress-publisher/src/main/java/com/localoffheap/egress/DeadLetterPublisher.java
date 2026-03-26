package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

public interface DeadLetterPublisher {
    void publish(StateChangeNotification notification, Exception failure);
}
