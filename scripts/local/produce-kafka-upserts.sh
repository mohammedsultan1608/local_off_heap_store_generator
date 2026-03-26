#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker-compose.local.yml exec -T kafka bash -lc "if command -v kafka-console-producer >/dev/null 2>&1; then KAFKA_PRODUCER_BIN=kafka-console-producer; else KAFKA_PRODUCER_BIN=kafka-console-producer.sh; fi; cat <<'EOF' | \$KAFKA_PRODUCER_BIN --bootstrap-server kafka:9092 --topic entity-upserts
{\"entityKey\":\"customer-1\",\"version\":2,\"operation\":\"UPSERT\",\"payload\":\"v2\",\"eventTimeEpochMs\":1700000000001,\"source\":\"KAFKA\",\"sourcePosition\":\"entity-upserts-0-1\"}
{\"entityKey\":\"customer-1\",\"version\":2,\"operation\":\"UPSERT\",\"payload\":\"v2\",\"eventTimeEpochMs\":1700000000002,\"source\":\"KAFKA\",\"sourcePosition\":\"entity-upserts-0-2\"}
{\"entityKey\":\"customer-1\",\"version\":1,\"operation\":\"UPSERT\",\"payload\":\"old\",\"eventTimeEpochMs\":1700000000003,\"source\":\"KAFKA\",\"sourcePosition\":\"entity-upserts-0-3\"}
EOF"
