-- https://www.h2database.com/html/datatypes.html

CREATE TABLE IF NOT EXISTS boards (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    owner_token VARCHAR(90) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_board_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS mock_rules (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    board_id VARCHAR(45) NOT NULL,
    method VARCHAR(20) NOT NULL,
    -- The allowed length is from 1 to 1,000,000,000 characters.
    -- The length is a size constraint; only the actual data is persisted.
    path VARCHAR NOT NULL,
    headers VARCHAR,
    body VARCHAR,
    status_code INT NOT NULL DEFAULT 200,
    delay INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_mock_board_id (board_id),
    INDEX idx_mock_created_at (created_at),
    CONSTRAINT fk_mock_rules_board
        FOREIGN KEY (board_id)
            REFERENCES boards(id)
                ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS webhooks (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    board_id VARCHAR(45) NOT NULL,
    method VARCHAR(20) NOT NULL,
    path VARCHAR NOT NULL,
    full_url VARCHAR NOT NULL,
    query_params VARCHAR,
    headers VARCHAR,
    body VARCHAR,
    content_type VARCHAR DEFAULT 'application/json',
    status_code INT NOT NULL,
    matched BOOL NOT NULL,
    received_at TIMESTAMP NOT NULL,
    processing_time_ms LONG NOT NULL,
    INDEX idx_webhook_board_id_key (board_id),
    CONSTRAINT fk_webhooks_board
        FOREIGN KEY (board_id)
            REFERENCES boards(id)
            ON DELETE CASCADE
);