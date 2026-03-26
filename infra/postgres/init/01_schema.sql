CREATE TABLE IF NOT EXISTS entity_snapshot (
    entity_key TEXT PRIMARY KEY,
    version BIGINT NOT NULL,
    payload BYTEA NOT NULL
);

CREATE TABLE IF NOT EXISTS snapshot_metadata (
    id INT PRIMARY KEY,
    watermark BIGINT NOT NULL
);

INSERT INTO snapshot_metadata (id, watermark)
VALUES (1, 0)
ON CONFLICT (id) DO NOTHING;
