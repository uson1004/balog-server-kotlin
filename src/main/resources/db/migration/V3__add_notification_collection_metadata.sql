CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    nickname VARCHAR(255),
    profile_image_url VARCHAR(255),
    email VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    push_notification_id BINARY(16),
    category VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,
    description VARCHAR(255),
    source VARCHAR(255) NOT NULL,
    raw_notification_text TEXT,
    idempotency_key VARCHAR(128),
    parser_version VARCHAR(64),
    source_notification_id VARCHAR(128),
    collected_at DATETIME(6),
    category_source VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    transaction_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_transaction_user_idempotency_key UNIQUE (user_id, idempotency_key)
);

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'transactions' AND column_name = 'idempotency_key');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE transactions ADD COLUMN idempotency_key VARCHAR(128) NULL', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'transactions' AND column_name = 'parser_version');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE transactions ADD COLUMN parser_version VARCHAR(64) NULL', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'transactions' AND column_name = 'source_notification_id');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE transactions ADD COLUMN source_notification_id VARCHAR(128) NULL', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'transactions' AND column_name = 'collected_at');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE transactions ADD COLUMN collected_at DATETIME(6) NULL', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'transactions' AND column_name = 'category_source');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE transactions ADD COLUMN category_source VARCHAR(32) NOT NULL DEFAULT ''MANUAL''', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @index_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'transactions' AND index_name = 'uk_transaction_user_idempotency_key');
SET @sql = IF(@index_exists = 0, 'ALTER TABLE transactions ADD CONSTRAINT uk_transaction_user_idempotency_key UNIQUE (user_id, idempotency_key)', 'SELECT 1');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
