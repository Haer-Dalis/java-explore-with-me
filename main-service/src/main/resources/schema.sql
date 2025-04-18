DROP TABLE IF EXISTS users, categories, event_location, events, requests, compilations, compilations_events, comments CASCADE;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_location (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    title VARCHAR(120) NOT NULL,
    state VARCHAR(63) NOT NULL,
    created_on TIMESTAMP,
    event_date TIMESTAMP,
    published_on TIMESTAMP,
    paid BOOLEAN,
    participant_limit INT,
    request_moderation BOOLEAN,
    confirmed_requests INT,
    views BIGINT,
    initiator_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES event_location (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    event_id BIGINT,
    requester_id BIGINT,
    created TIMESTAMP,
    status VARCHAR(255),
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT generated BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS compilations_events (
    event_id BIGINT,
    compilation_id BIGINT,
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    FOREIGN KEY (compilation_id) REFERENCES compilations (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY 	NOT NULL,
    user_id	BIGINT NOT NULL,
    event_id BIGINT	NOT NULL,
    message	VARCHAR(500) NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments_id PRIMARY KEY (id),
    CONSTRAINT comments_user_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_comments_event FOREIGN KEY (event_id) REFERENCES events (id)
);