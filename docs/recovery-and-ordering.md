# Recovery and Ordering

## Startup Sequence

1. Load latest snapshot rows and snapshot watermark from PostgreSQL.
2. Populate off-heap store entries with snapshot versions/payloads.
3. Load last durable source checkpoints.
4. Start ingress consumers from checkpoint (or watermark-derived defaults).
5. Process events through ordering gate and apply engine.

## Ordering Policy

- Processing is deterministic inside a single apply loop.
- Per-entity expected monotonic version policy:
  - New entity: accept first observed version.
  - Existing entity:
    - `incomingVersion > currentVersion`: accept.
    - `incomingVersion == currentVersion` with same payload hash: idempotent duplicate, drop as duplicate.
    - `incomingVersion <= currentVersion` otherwise: stale/conflicting, reject and record metric.

## Replay Boundary Policy

- Snapshot captures a source watermark.
- Consumers start at checkpoint >= snapshot watermark.
- If checkpoint is missing, replay from configured bootstrap position.
- Duplicate protection ensures replayed already-applied events do not mutate state.

## Failure Handling

- Apply failures do not advance checkpoint.
- Publisher failure policy:
  - Retry with bounded attempts.
  - Route exhausted Kafka publish failures to DLQ path.
  - Aeron transient errors are retried with backoff.
- On crash/restart, resume from durable checkpoints.

## Determinism Guarantees

- One canonical event model regardless of transport.
- Version gate is source-agnostic.
- Store update and checkpoint update happen in a single ordered commit section in the service loop.
