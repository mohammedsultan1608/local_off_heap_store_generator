package com.localoffheap.service;

import com.localoffheap.core.apply.UpsertApplyEngine;
import com.localoffheap.core.checkpoint.InMemoryCheckpointStore;
import com.localoffheap.core.ingress.EventSource;
import com.localoffheap.core.ordering.OrderingAndVersionGate;
import com.localoffheap.core.store.DirectByteBufferOffHeapStore;
import com.localoffheap.egress.AeronNotificationPublisher;
import com.localoffheap.egress.InMemoryDeadLetterPublisher;
import com.localoffheap.egress.KafkaNotificationPublisher;
import com.localoffheap.egress.NotificationPublisher;
import com.localoffheap.egress.RealAeronNotificationPublisher;
import com.localoffheap.egress.RetryingStateChangePublisher;
import com.localoffheap.ingress.aeron.RealAeronIngressAdapter;
import com.localoffheap.ingress.aeron.AeronIngressAdapter;
import com.localoffheap.ingress.kafka.KafkaIngressAdapter;
import com.localoffheap.snapshot.InMemoryPostgresSnapshotRepository;
import com.localoffheap.snapshot.PostgresSnapshotLoader;
import com.localoffheap.snapshot.SnapshotRow;
import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;

import java.time.Clock;
import java.util.List;

public final class LocalOffHeapStoreApplication {

    private LocalOffHeapStoreApplication() {
    }

    public static void main(String[] args) {
        DirectByteBufferOffHeapStore store = new DirectByteBufferOffHeapStore();
        RuntimeHandles runtimeHandles = RuntimeHandles.startFromEnvironment();
        PostgresSnapshotLoader snapshotLoader = new PostgresSnapshotLoader(
                new InMemoryPostgresSnapshotRepository(List.of(
                        new SnapshotRow("entity-1", 1, "seed".getBytes())
                ), 100L),
                store
        );

        EventSource aeronIngress = runtimeHandles.realAeron
                ? runtimeHandles.realAeronIngress
                : new AeronIngressAdapter();
        KafkaIngressAdapter kafkaIngress = new KafkaIngressAdapter();
        NotificationPublisher aeronPublisher = runtimeHandles.realAeron
                ? runtimeHandles.realAeronPublisher
                : new AeronNotificationPublisher();

        LocalOffHeapStoreService service = new LocalOffHeapStoreService(
                snapshotLoader,
                aeronIngress,
                kafkaIngress,
                new UpsertApplyEngine(store, new OrderingAndVersionGate(), Clock.systemUTC()),
                new RetryingStateChangePublisher(
                        aeronPublisher,
                        new KafkaNotificationPublisher(),
                        new InMemoryDeadLetterPublisher(),
                        2
                ),
                new InMemoryCheckpointStore(),
                1024,
                new ServiceMetrics()
        );

        service.bootstrap();
        System.out.println("Local Off-Heap Store service initialized. Aeron mode=" +
                (runtimeHandles.realAeron ? "REAL" : "INMEMORY"));

        if (runtimeHandles.realAeron) {
            Runtime.getRuntime().addShutdownHook(new Thread(runtimeHandles::close));
            while (true) {
                service.ingestOnce(256);
                service.processOnce(256);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static final class RuntimeHandles implements AutoCloseable {
        private final boolean realAeron;
        private final MediaDriver mediaDriver;
        private final Aeron aeron;
        private final RealAeronIngressAdapter realAeronIngress;
        private final RealAeronNotificationPublisher realAeronPublisher;

        private RuntimeHandles(
                boolean realAeron,
                MediaDriver mediaDriver,
                Aeron aeron,
                RealAeronIngressAdapter realAeronIngress,
                RealAeronNotificationPublisher realAeronPublisher) {
            this.realAeron = realAeron;
            this.mediaDriver = mediaDriver;
            this.aeron = aeron;
            this.realAeronIngress = realAeronIngress;
            this.realAeronPublisher = realAeronPublisher;
        }

        static RuntimeHandles startFromEnvironment() {
            String mode = env("AERON_MODE", "INMEMORY");
            if (!"REAL".equalsIgnoreCase(mode)) {
                return new RuntimeHandles(false, null, null, null, null);
            }

            MediaDriver mediaDriver = null;
            if (Boolean.parseBoolean(env("AERON_EMBEDDED_DRIVER", "true"))) {
                MediaDriver.Context driverContext = new MediaDriver.Context()
                        .dirDeleteOnStart(true)
                        .dirDeleteOnShutdown(true)
                        .aeronDirectoryName(env("AERON_DIR", "/tmp/aeron-local-offheap"));
                mediaDriver = MediaDriver.launchEmbedded(driverContext);
            }

            Aeron.Context aeronContext = new Aeron.Context()
                    .aeronDirectoryName(env("AERON_DIR", "/tmp/aeron-local-offheap"));
            Aeron aeron = Aeron.connect(aeronContext);

            String ingressChannel = env("AERON_INGRESS_CHANNEL", "aeron:udp?endpoint=localhost:20121");
            int ingressStreamId = Integer.parseInt(env("AERON_INGRESS_STREAM_ID", "1001"));
            String egressChannel = env("AERON_EGRESS_CHANNEL", "aeron:udp?endpoint=localhost:20122");
            int egressStreamId = Integer.parseInt(env("AERON_EGRESS_STREAM_ID", "1002"));

            return new RuntimeHandles(
                    true,
                    mediaDriver,
                    aeron,
                    new RealAeronIngressAdapter(aeron, ingressChannel, ingressStreamId, 256),
                    new RealAeronNotificationPublisher(aeron, egressChannel, egressStreamId, 1000)
            );
        }

        @Override
        public void close() {
            if (realAeronPublisher != null) {
                realAeronPublisher.close();
            }
            if (realAeronIngress != null) {
                realAeronIngress.close();
            }
            if (aeron != null) {
                aeron.close();
            }
            if (mediaDriver != null) {
                mediaDriver.close();
            }
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
