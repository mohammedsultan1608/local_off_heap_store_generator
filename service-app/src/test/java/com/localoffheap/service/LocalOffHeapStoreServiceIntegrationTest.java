package com.localoffheap.service;

import com.localoffheap.core.apply.UpsertApplyEngine;
import com.localoffheap.core.checkpoint.InMemoryCheckpointStore;
import com.localoffheap.core.model.OperationType;
import com.localoffheap.core.model.SourceType;
import com.localoffheap.core.model.UpsertEvent;
import com.localoffheap.core.ordering.OrderingAndVersionGate;
import com.localoffheap.core.store.DirectByteBufferOffHeapStore;
import com.localoffheap.egress.AeronNotificationPublisher;
import com.localoffheap.egress.InMemoryDeadLetterPublisher;
import com.localoffheap.egress.KafkaNotificationPublisher;
import com.localoffheap.egress.RetryingStateChangePublisher;
import com.localoffheap.ingress.aeron.AeronIngressAdapter;
import com.localoffheap.ingress.kafka.KafkaIngressAdapter;
import com.localoffheap.snapshot.InMemoryPostgresSnapshotRepository;
import com.localoffheap.snapshot.PostgresSnapshotLoader;
import com.localoffheap.snapshot.SnapshotRow;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalOffHeapStoreServiceIntegrationTest {

    @Test
    void snapshotReplayAndDualPublishAreDeterministic() {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        AeronIngressAdapter aeronIngress = new AeronIngressAdapter();
        KafkaIngressAdapter kafkaIngress = new KafkaIngressAdapter();
        AeronNotificationPublisher aeronOut = new AeronNotificationPublisher();
        KafkaNotificationPublisher kafkaOut = new KafkaNotificationPublisher();
        InMemoryCheckpointStore checkpoints = new InMemoryCheckpointStore();
        LocalOffHeapStoreService service = new LocalOffHeapStoreService(
                new PostgresSnapshotLoader(
                        new InMemoryPostgresSnapshotRepository(List.of(
                                new SnapshotRow("account-1", 1, "seed".getBytes())
                        ), 10L),
                        store
                ),
                aeronIngress,
                kafkaIngress,
                new UpsertApplyEngine(store, new OrderingAndVersionGate(), Clock.systemUTC()),
                new RetryingStateChangePublisher(aeronOut, kafkaOut, new InMemoryDeadLetterPublisher(), 2),
                checkpoints,
                32,
                new ServiceMetrics()
        );

        service.bootstrap();
        kafkaIngress.accept(event("account-1", 2, "v2", SourceType.KAFKA, "topic-0-2"));
        aeronIngress.accept(event("account-1", 2, "v2", SourceType.AERON, "aeron-2"));
        aeronIngress.accept(event("account-1", 1, "stale", SourceType.AERON, "aeron-1"));

        service.ingestOnce(10);
        service.processOnce(20);

        assertThat(store.get("account-1")).isPresent();
        assertThat(store.get("account-1").orElseThrow().version()).isEqualTo(2);
        assertThat(aeronOut.published()).hasSize(1);
        assertThat(kafkaOut.published()).hasSize(1);
        assertThat(service.metrics().applied()).isEqualTo(1);
        assertThat(service.metrics().duplicates()).isEqualTo(1);
        assertThat(service.metrics().stale()).isEqualTo(1);
        assertThat(checkpoints.get(SourceType.KAFKA)).isPresent();
    }

    @Test
    void publishFailureDoesNotAdvanceCheckpoint() {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        KafkaIngressAdapter kafkaIngress = new KafkaIngressAdapter();
        InMemoryCheckpointStore checkpoints = new InMemoryCheckpointStore();
        AeronNotificationPublisher aeronOut = new AeronNotificationPublisher();
        aeronOut.failNextPublish();

        LocalOffHeapStoreService service = new LocalOffHeapStoreService(
                new PostgresSnapshotLoader(
                        new InMemoryPostgresSnapshotRepository(List.of(), 10L),
                        store
                ),
                new AeronIngressAdapter(),
                kafkaIngress,
                new UpsertApplyEngine(store, new OrderingAndVersionGate(), Clock.systemUTC()),
                new RetryingStateChangePublisher(
                        aeronOut,
                        new KafkaNotificationPublisher(),
                        new InMemoryDeadLetterPublisher(),
                        0
                ),
                checkpoints,
                8,
                new ServiceMetrics()
        );

        service.bootstrap();
        kafkaIngress.accept(event("k1", 1, "v1", SourceType.KAFKA, "topic-0-1"));
        service.ingestOnce(10);

        assertThatThrownBy(() -> service.processOnce(10)).isInstanceOf(RuntimeException.class);
        assertThat(checkpoints.get(SourceType.KAFKA)).isEmpty();
        assertThat(service.metrics().publishFailures()).isEqualTo(1);
    }

    @Test
    void performanceSmokeTestProcessesBurst() {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        KafkaIngressAdapter kafkaIngress = new KafkaIngressAdapter();
        LocalOffHeapStoreService service = new LocalOffHeapStoreService(
                new PostgresSnapshotLoader(
                        new InMemoryPostgresSnapshotRepository(List.of(), 0L),
                        store
                ),
                new AeronIngressAdapter(),
                kafkaIngress,
                new UpsertApplyEngine(store, new OrderingAndVersionGate(), Clock.systemUTC()),
                new RetryingStateChangePublisher(
                        new AeronNotificationPublisher(),
                        new KafkaNotificationPublisher(),
                        new InMemoryDeadLetterPublisher(),
                        1
                ),
                new InMemoryCheckpointStore(),
                20_000,
                new ServiceMetrics()
        );
        service.bootstrap();

        int n = 10_000;
        for (int i = 1; i <= n; i++) {
            kafkaIngress.accept(event("entity-" + i, 1, "v", SourceType.KAFKA, "topic-0-" + i));
        }

        long start = System.nanoTime();
        service.ingestOnce(n);
        int processed = service.processOnce(n);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

        assertThat(processed).isEqualTo(n);
        assertThat(service.metrics().applied()).isEqualTo(n);
        assertThat(elapsedMs).isLessThan(10_000L);
    }

    private UpsertEvent event(String key, long version, String payload, SourceType sourceType, String sourcePos) {
        return new UpsertEvent(key, version, OperationType.UPSERT, payload.getBytes(), 1L, sourceType, sourcePos);
    }
}
