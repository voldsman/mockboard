-- https://www.h2database.com/html/datatypes.html

CREATE TABLE IF NOT EXISTS boards (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    api_key VARCHAR(45) UNIQUE NOT NULL,
    owner_token VARCHAR(90) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_board_api (api_key),
    INDEX idx_board_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS mock_rules (
    id VARCHAR(45) NOT NULL PRIMARY KEY,
    board_id VARCHAR(45) NOT NULL,
    api_key VARCHAR(45) NOT NULL,
    method VARCHAR(20) NOT NULL,
    -- The allowed length is from 1 to 1,000,000,000 characters.
    -- The length is a size constraint; only the actual data is persisted.
    path VARCHAR NOT NULL,
    headers VARCHAR,
    body VARCHAR,
    status_code INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_mock_board_id (board_id),
    INDEX idx_mock_api (api_key),
    INDEX idx_mock_created_at (created_at),
    CONSTRAINT fk_mock_rules_board
        FOREIGN KEY (board_id)
            REFERENCES boards(id)
                ON DELETE CASCADE
);