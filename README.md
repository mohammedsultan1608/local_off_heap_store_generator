# Local Off-Heap Store Generator

Java single-node service that:

- Loads initial entity snapshot from PostgreSQL persistence.
- Consumes versioned upsert events from Aeron and Kafka adapters.
- Applies deterministic ordered updates to an off-heap materialized view.
- Publishes state-change notifications to Aeron and Kafka with retry and DLQ behavior.

## Modules

- `core-domain`: event models, off-heap store, ordering gate, apply engine, checkpoints.
- `snapshot-postgres`: snapshot repository abstraction and loader.
- `ingress-aeron`: Aeron ingress adapter.
- `ingress-kafka`: Kafka ingress adapter.
- `egress-publisher`: dual publisher with retry and DLQ handling.
- `service-app`: runtime orchestration and integration tests.

## Build

Install Gradle 8+ locally, then run:

```bash
gradle clean test
```

Quick runtime validation without Gradle:

```bash
javac core-domain/src/main/java/**/*.java ingress-aeron/src/main/java/**/*.java ingress-kafka/src/main/java/**/*.java snapshot-postgres/src/main/java/**/*.java egress-publisher/src/main/java/**/*.java service-app/src/main/java/**/*.java -d out/classes
java -cp out/classes com.localoffheap.service.ValidationHarness
```

## Current Status

- Phase 0 docs completed in `docs/`.
- Deterministic apply path, checkpointing, and dual publish implemented.
- Unit + integration + fault/performance smoke tests added.

## Local Real Integration Setup

For Docker-based local Kafka + PostgreSQL setup and test flow, see:

- `docs/local-real-integration.md`

## Aeron Mode

- Default mode: `AERON_MODE=INMEMORY` (PoC in-memory adapters).
- Real mode: `AERON_MODE=REAL` to use Aeron client + embedded MediaDriver wiring.
