package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;

import java.util.ArrayList;
import java.util.List;

public final class InMemoryDeadLetterPublisher implements DeadLetterPublisher {
    private final List<StateChangeNotification> deadLetters = new ArrayList<>();

    @Override
    public synchronized void publish(StateChangeNotification notification, Exception failure) {
        deadLetters.add(notification);
    }

    public synchronized List<StateChangeNotification> deadLetters() {
        return List.copyOf(deadLetters);
    }
}
