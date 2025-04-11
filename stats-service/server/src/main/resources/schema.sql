DROP TABLE IF EXISTS stats;

CREATE TABLE IF NOT EXISTS stats (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    ip VARCHAR(63) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE
);