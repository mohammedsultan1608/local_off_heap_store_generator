# Local Real Integration Guide (Kafka + Postgres + Aeron-ready)

This guide gives you a practical local setup to validate the external dependencies needed by the project:

- PostgreSQL snapshot source
- Kafka upsert ingress and change-topic egress
- Aeron integration preparation path

## 1) Prerequisites

- Docker + Docker Compose installed
- Java installed

## 2) Start local infrastructure

From project root:

```bash
bash scripts/local/up.sh
```

This starts:

- PostgreSQL on `localhost:5432`
- Kafka on `localhost:9094` (advertised externally)

The script waits until both services are actually ready before returning.

## 3) Create Kafka topics

```bash
bash scripts/local/create-kafka-topics.sh
```

Topics created:

- `entity-upserts`
- `state-changes`

## 4) Seed PostgreSQL snapshot

```bash
bash scripts/local/seed-postgres.sh
```

This inserts baseline entities and sets snapshot watermark to `100`.

## 5) Produce test upsert events to Kafka

```bash
bash scripts/local/produce-kafka-upserts.sh
```

This sends:

- one valid version bump
- one duplicate
- one stale update

## 6) Read back Kafka events

```bash
bash scripts/local/consume-kafka-topic.sh entity-upserts
```

Use this to verify producer + topic + broker connectivity.
Note: helper scripts run Kafka CLI inside the Kafka container and connect via `kafka:9092`.

## 7) Run current PoC service validation

```bash
javac core-domain/src/main/java/**/*.java ingress-aeron/src/main/java/**/*.java ingress-kafka/src/main/java/**/*.java snapshot-postgres/src/main/java/**/*.java egress-publisher/src/main/java/**/*.java service-app/src/main/java/**/*.java -d out/classes
java -cp out/classes com.localoffheap.service.ValidationHarness
```

Expected output:

- `Validation harness passed all scenarios.`

## 8) Aeron notes for local integration

The service now supports a real Aeron mode.

### Run service with real Aeron adapters

```bash
AERON_MODE=REAL \
AERON_EMBEDDED_DRIVER=true \
AERON_DIR=/tmp/aeron-local-offheap \
AERON_INGRESS_CHANNEL='aeron:udp?endpoint=localhost:20121' \
AERON_INGRESS_STREAM_ID=1001 \
AERON_EGRESS_CHANNEL='aeron:udp?endpoint=localhost:20122' \
AERON_EGRESS_STREAM_ID=1002 \
gradle :service-app:run
```

Notes:

- In `REAL` mode the app starts an embedded MediaDriver (unless `AERON_EMBEDDED_DRIVER=false`).
- In default mode (`AERON_MODE=INMEMORY`) it keeps using in-memory Aeron adapters for PoC tests.
- Aeron wire payload uses a simple text codec mapped to canonical `UpsertEvent` and `StateChangeNotification`.

## 9) Teardown

```bash
bash scripts/local/down.sh
```
