package com.localoffheap.egress;

import com.localoffheap.core.model.StateChangeNotification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetryingStateChangePublisherTest {
    @Test
    void retriesAndRoutesKafkaFailuresToDlq() {
        AeronNotificationPublisher aeron = new AeronNotificationPublisher();
        KafkaNotificationPublisher kafka = new KafkaNotificationPublisher();
        InMemoryDeadLetterPublisher dlq = new InMemoryDeadLetterPublisher();
        kafka.failTimes(10);

        RetryingStateChangePublisher publisher =
                new RetryingStateChangePublisher(aeron, kafka, dlq, 2);

        publisher.publish(new StateChangeNotification("k", 1, 2, "UPSERT_APPLIED", 1L, "v2".getBytes()));

        assertThat(aeron.published()).hasSize(1);
        assertThat(kafka.published()).isEmpty();
        assertThat(dlq.deadLetters()).hasSize(1);
    }
}
