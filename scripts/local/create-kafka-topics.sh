#!/usr/bin/env bash
set -euo pipefail

for i in {1..60}; do
  if docker compose -f docker-compose.local.yml exec -T kafka bash -lc \
    'echo > /dev/tcp/kafka/9092' >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "Kafka is still not reachable; aborting topic creation."
    docker compose -f docker-compose.local.yml logs kafka --tail 120
    exit 1
  fi
  echo "Kafka not ready yet, retrying..."
  sleep 2
done

docker compose -f docker-compose.local.yml exec -T kafka bash -lc \
  'if command -v kafka-topics >/dev/null 2>&1; then KAFKA_TOPICS_BIN=kafka-topics; else KAFKA_TOPICS_BIN=kafka-topics.sh; fi; $KAFKA_TOPICS_BIN --bootstrap-server kafka:9092 --create --if-not-exists --topic entity-upserts --partitions 1 --replication-factor 1; $KAFKA_TOPICS_BIN --bootstrap-server kafka:9092 --create --if-not-exists --topic state-changes --partitions 1 --replication-factor 1; $KAFKA_TOPICS_BIN --bootstrap-server kafka:9092 --list'
