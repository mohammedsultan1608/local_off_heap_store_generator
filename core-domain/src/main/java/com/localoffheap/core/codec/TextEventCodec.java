package com.localoffheap.core.codec;

import com.localoffheap.core.model.OperationType;
import com.localoffheap.core.model.SourceType;
import com.localoffheap.core.model.StateChangeNotification;
import com.localoffheap.core.model.UpsertEvent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TextEventCodec {
    private static final String DELIMITER = "|";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private TextEventCodec() {
    }

    public static byte[] encodeUpsertEvent(UpsertEvent event) {
        String encoded = String.join(DELIMITER,
                event.entityKey(),
                Long.toString(event.version()),
                event.operationType().name(),
                ENCODER.encodeToString(event.payload()),
                Long.toString(event.eventTimeEpochMs()),
                event.sourceType().name(),
                event.sourcePosition()
        );
        return encoded.getBytes(StandardCharsets.UTF_8);
    }

    public static UpsertEvent decodeUpsertEvent(byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid UpsertEvent payload format");
        }

        return new UpsertEvent(
                parts[0],
                Long.parseLong(parts[1]),
                OperationType.valueOf(parts[2]),
                DECODER.decode(parts[3]),
                Long.parseLong(parts[4]),
                SourceType.valueOf(parts[5]),
                parts[6]
        );
    }

    public static byte[] encodeStateChangeNotification(StateChangeNotification notification) {
        String encoded = String.join(DELIMITER,
                notification.entityKey(),
                Long.toString(notification.oldVersion()),
                Long.toString(notification.newVersion()),
                notification.changeType(),
                Long.toString(notification.appliedAtEpochMs()),
                ENCODER.encodeToString(notification.payload())
        );
        return encoded.getBytes(StandardCharsets.UTF_8);
    }

    public static StateChangeNotification decodeStateChangeNotification(byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid StateChangeNotification payload format");
        }

        return new StateChangeNotification(
                parts[0],
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                parts[3],
                Long.parseLong(parts[4]),
                DECODER.decode(parts[5])
        );
    }
}
