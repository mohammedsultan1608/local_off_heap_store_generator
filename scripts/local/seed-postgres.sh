#!/usr/bin/env bash
set -euo pipefail

until docker compose -f docker-compose.local.yml exec -T postgres \
  pg_isready -U local_user -d offheap_store >/dev/null 2>&1; do
  echo "Postgres not ready yet, retrying..."
  sleep 1
done

docker compose -f docker-compose.local.yml exec -T postgres psql \
  -U local_user -d offheap_store <<'SQL'
INSERT INTO entity_snapshot (entity_key, version, payload)
VALUES
  ('customer-1', 1, decode('73656564', 'hex')),
  ('customer-2', 3, decode('736565642d32', 'hex'))
ON CONFLICT (entity_key) DO UPDATE
SET version = excluded.version, payload = excluded.payload;

UPDATE snapshot_metadata
SET watermark = 100
WHERE id = 1;
SQL

docker compose -f docker-compose.local.yml exec -T postgres psql \
  -U local_user -d offheap_store \
  -c "SELECT entity_key, version, encode(payload, 'escape') AS payload_text FROM entity_snapshot ORDER BY entity_key;"
