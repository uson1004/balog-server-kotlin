ALTER TABLE integration_outbox
    ADD COLUMN lease_id BINARY(16) NULL,
    ADD COLUMN lease_expires_at DATETIME(6) NULL,
    ADD INDEX idx_integration_outbox_status_created_at (status, created_at);
