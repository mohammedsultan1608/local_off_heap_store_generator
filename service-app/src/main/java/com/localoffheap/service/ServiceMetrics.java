package com.localoffheap.service;

import java.util.concurrent.atomic.AtomicLong;

public final class ServiceMetrics {
    private final AtomicLong applied = new AtomicLong();
    private final AtomicLong duplicates = new AtomicLong();
    private final AtomicLong stale = new AtomicLong();
    private final AtomicLong ingressBackpressureDrops = new AtomicLong();
    private final AtomicLong publishFailures = new AtomicLong();

    public void incrementApplied() {
        applied.incrementAndGet();
    }

    public void incrementDuplicates() {
        duplicates.incrementAndGet();
    }

    public void incrementStale() {
        stale.incrementAndGet();
    }

    public void incrementIngressBackpressureDrops() {
        ingressBackpressureDrops.incrementAndGet();
    }

    public void incrementPublishFailures() {
        publishFailures.incrementAndGet();
    }

    public long applied() {
        return applied.get();
    }

    public long duplicates() {
        return duplicates.get();
    }

    public long stale() {
        return stale.get();
    }

    public long ingressBackpressureDrops() {
        return ingressBackpressureDrops.get();
    }

    public long publishFailures() {
        return publishFailures.get();
    }
}
