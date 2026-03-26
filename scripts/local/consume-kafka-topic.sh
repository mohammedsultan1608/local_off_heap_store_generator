#!/usr/bin/env bash
set -euo pipefail

TOPIC="${1:-entity-upserts}"

docker compose -f docker-compose.local.yml exec -T kafka bash -lc \
  "if command -v kafka-console-consumer >/dev/null 2>&1; then KAFKA_CONSUMER_BIN=kafka-console-consumer; else KAFKA_CONSUMER_BIN=kafka-console-consumer.sh; fi; \$KAFKA_CONSUMER_BIN --bootstrap-server kafka:9092 --topic \"$TOPIC\" --from-beginning --timeout-ms 5000"
