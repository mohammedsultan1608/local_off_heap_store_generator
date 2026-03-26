package com.localoffheap.service;

import com.localoffheap.core.apply.ApplyResult;
import com.localoffheap.core.apply.ApplyStatus;
import com.localoffheap.core.apply.UpsertApplyEngine;
import com.localoffheap.core.checkpoint.CheckpointStore;
import com.localoffheap.core.checkpoint.SourceCheckpoint;
import com.localoffheap.core.ingress.EventSource;
import com.localoffheap.core.model.UpsertEvent;
import com.localoffheap.egress.RetryingStateChangePublisher;
import com.localoffheap.snapshot.PostgresSnapshotLoader;
import com.localoffheap.snapshot.SnapshotLoadResult;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LocalOffHeapStoreService {
    private final PostgresSnapshotLoader snapshotLoader;
    private final EventSource aeronIngress;
    private final EventSource kafkaIngress;
    private final UpsertApplyEngine applyEngine;
    private final RetryingStateChangePublisher publisher;
    private final CheckpointStore checkpointStore;
    private final BlockingQueue<UpsertEvent> applyQueue;
    private final ServiceMetrics metrics;
    private volatile boolean bootstrapped;

    public LocalOffHeapStoreService(
            PostgresSnapshotLoader snapshotLoader,
            EventSource aeronIngress,
            EventSource kafkaIngress,
            UpsertApplyEngine applyEngine,
            RetryingStateChangePublisher publisher,
            CheckpointStore checkpointStore,
            int queueCapacity,
            ServiceMetrics metrics) {
        this.snapshotLoader = snapshotLoader;
        this.aeronIngress = aeronIngress;
        this.kafkaIngress = kafkaIngress;
        this.applyEngine = applyEngine;
        this.publisher = publisher;
        this.checkpointStore = checkpointStore;
        this.applyQueue = new ArrayBlockingQueue<>(queueCapacity);
        this.metrics = metrics;
    }

    public SnapshotLoadResult bootstrap() {
        SnapshotLoadResult result = snapshotLoader.load();
        bootstrapped = true;
        return result;
    }

    public int ingestOnce(int maxBatchPerSource) {
        int accepted = 0;
        accepted += offerAll(aeronIngress.poll(maxBatchPerSource));
        accepted += offerAll(kafkaIngress.poll(maxBatchPerSource));
        return accepted;
    }

    public int processOnce(int maxEvents) {
        int processed = 0;
        for (int i = 0; i < maxEvents; i++) {
            UpsertEvent event = applyQueue.poll();
            if (event == null) {
                break;
            }
            processed++;
            ApplyResult result = applyEngine.apply(event);
            if (result.status() == ApplyStatus.APPLIED) {
                try {
                    publisher.publish(result.notification().orElseThrow());
                    metrics.incrementApplied();
                } catch (RuntimeException ex) {
                    metrics.incrementPublishFailures();
                    throw ex;
                }
            } else if (result.status() == ApplyStatus.DUPLICATE) {
                metrics.incrementDuplicates();
            } else {
                metrics.incrementStale();
            }
            checkpointStore.save(new SourceCheckpoint(event.sourceType(), event.sourcePosition()));
        }
        return processed;
    }

    private int offerAll(List<UpsertEvent> events) {
        int accepted = 0;
        for (UpsertEvent event : events) {
            if (applyQueue.offer(event)) {
                accepted++;
            } else {
                metrics.incrementIngressBackpressureDrops();
            }
        }
        return accepted;
    }

    public ServiceMetrics metrics() {
        return metrics;
    }

    public int queueSize() {
        return applyQueue.size();
    }

    public boolean isReady() {
        return bootstrapped;
    }
}
