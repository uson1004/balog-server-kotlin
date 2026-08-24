CREATE TABLE IF NOT EXISTS integration_outbox (
    id BINARY(16) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    connection_id VARCHAR(255) NOT NULL,
    connector_type VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(255) NOT NULL,
    attempt_count INT NOT NULL,
    next_attempt_at DATETIME(6),
    last_error TEXT,
    delivered_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_integration_outbox_event_connection UNIQUE (event_id, connection_id)
);
