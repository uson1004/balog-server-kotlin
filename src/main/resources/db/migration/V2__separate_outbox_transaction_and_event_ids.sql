ALTER TABLE integration_outbox
    ADD COLUMN transaction_id VARCHAR(255) NULL AFTER id;

UPDATE integration_outbox
SET transaction_id = event_id
WHERE transaction_id IS NULL;

ALTER TABLE integration_outbox
    MODIFY COLUMN transaction_id VARCHAR(255) NOT NULL;

ALTER TABLE integration_outbox
    DROP INDEX uk_integration_outbox_event_connection;

ALTER TABLE integration_outbox
    ADD CONSTRAINT uk_integration_outbox_transaction_connection
        UNIQUE (transaction_id, connection_id);
