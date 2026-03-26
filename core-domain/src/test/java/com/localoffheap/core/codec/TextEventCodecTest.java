package com.localoffheap.core.codec;

import com.localoffheap.core.model.OperationType;
import com.localoffheap.core.model.SourceType;
import com.localoffheap.core.model.StateChangeNotification;
import com.localoffheap.core.model.UpsertEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextEventCodecTest {
    @Test
    void roundTripsUpsertEvent() {
        UpsertEvent original = new UpsertEvent(
                "entity-1",
                10L,
                OperationType.UPSERT,
                "payload".getBytes(),
                123L,
                SourceType.AERON,
                "source-pos"
        );

        byte[] bytes = TextEventCodec.encodeUpsertEvent(original);
        UpsertEvent decoded = TextEventCodec.decodeUpsertEvent(bytes);

        assertThat(decoded.entityKey()).isEqualTo(original.entityKey());
        assertThat(decoded.version()).isEqualTo(original.version());
        assertThat(decoded.operationType()).isEqualTo(original.operationType());
        assertThat(decoded.payload()).isEqualTo(original.payload());
        assertThat(decoded.eventTimeEpochMs()).isEqualTo(original.eventTimeEpochMs());
        assertThat(decoded.sourceType()).isEqualTo(original.sourceType());
        assertThat(decoded.sourcePosition()).isEqualTo(original.sourcePosition());
    }

    @Test
    void roundTripsStateChange() {
        StateChangeNotification original = new StateChangeNotification(
                "entity-1",
                9L,
                10L,
                "UPSERT_APPLIED",
                999L,
                "after".getBytes()
        );

        byte[] bytes = TextEventCodec.encodeStateChangeNotification(original);
        StateChangeNotification decoded = TextEventCodec.decodeStateChangeNotification(bytes);

        assertThat(decoded.entityKey()).isEqualTo(original.entityKey());
        assertThat(decoded.oldVersion()).isEqualTo(original.oldVersion());
        assertThat(decoded.newVersion()).isEqualTo(original.newVersion());
        assertThat(decoded.changeType()).isEqualTo(original.changeType());
        assertThat(decoded.appliedAtEpochMs()).isEqualTo(original.appliedAtEpochMs());
        assertThat(decoded.payload()).isEqualTo(original.payload());
    }
}
