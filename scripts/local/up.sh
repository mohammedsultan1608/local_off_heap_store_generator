#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps

echo "Waiting for postgres readiness..."
for i in {1..60}; do
  if docker compose -f docker-compose.local.yml exec -T postgres \
    pg_isready -U local_user -d offheap_store >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "Postgres did not become ready in time."
    docker compose -f docker-compose.local.yml logs postgres --tail 80
    exit 1
  fi
  sleep 1
done

echo "Waiting for kafka readiness..."
for i in {1..90}; do
  if docker compose -f docker-compose.local.yml exec -T kafka bash -lc \
    'echo > /dev/tcp/kafka/9092' >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 90 ]]; then
    echo "Kafka did not become ready in time."
    docker compose -f docker-compose.local.yml logs kafka --tail 120
    exit 1
  fi
  sleep 2
done

echo "All local services are ready."
