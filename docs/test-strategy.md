# Test Strategy

## Unit Tests

- Version gate:
  - accept strictly newer versions
  - drop duplicates
  - reject stale/conflicting versions
- Apply engine:
  - writes accepted state into off-heap store
  - returns change notifications only for applied updates
- Checkpoint manager:
  - update and read correctness

## Integration Tests

- Snapshot restore + replay:
  - seed snapshot
  - feed events including stale and duplicate records
  - verify final state and published change stream
- Dual publisher:
  - verify Aeron and Kafka adapters both receive identical normalized change events

## Fault Injection Tests

- Crash between apply and checkpoint persist.
- Publisher transient failures and retry exhaustion.
- Duplicate bursts and out-of-order sequences.

## Performance Validation

- Throughput benchmark for apply loop.
- p50/p95/p99 latency for event-to-apply and apply-to-publish.
- Off-heap memory growth profile under sustained load.

## Release Gate

- All critical unit and integration tests pass in CI.
- Fault-injection suite completes with expected behavior.
- Baseline performance targets documented and reproducible.
