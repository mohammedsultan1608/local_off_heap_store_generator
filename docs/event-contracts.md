# Event Contracts

## Canonical Upsert Event

All ingress adapters map source-specific payloads into this internal model.

```json
{
  "entityKey": "string",
  "version": 123,
  "operation": "UPSERT",
  "payload": "byte[]",
  "eventTimeEpochMs": 1700000000000,
  "source": "AERON|KAFKA",
  "sourcePosition": "string"
}
```

## Rules

- `entityKey` must be non-empty.
- `version` must be positive and monotonic per entity.
- `operation` currently supports `UPSERT` only.
- `payload` is opaque to the core engine and interpreted by domain-specific codecs.
- `sourcePosition` captures transport replay cursor (Kafka topic/partition/offset or Aeron position/session metadata).

## State Change Notification Contract

Published after successful apply:

```json
{
  "entityKey": "string",
  "oldVersion": 122,
  "newVersion": 123,
  "changeType": "UPSERT_APPLIED",
  "appliedAtEpochMs": 1700000000001,
  "payload": "byte[]"
}
```

## Compatibility Strategy

- Additive-only changes for optional fields.
- Required field changes are major-version events.
- Unknown optional fields must be ignored by readers.
- Schema contract tests enforce backward compatibility for sample fixtures.
