-- Users table to store registered users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    elo INTEGER DEFAULT 1000,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User profile table (minimal implementation for editable profile page)
CREATE TABLE user_profiles (
    user_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    display_name VARCHAR(100)
);

-- Push-up records table to store each push-up session
CREATE TABLE pushup_records (
    record_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    count INTEGER NOT NULL CHECK (count >= 0),
    record_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tournaments table to track 2-minute tournaments
CREATE TABLE tournaments (
    tournament_id SERIAL PRIMARY KEY,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED'))
);

-- Tournament participants table
CREATE TABLE tournament_participants (
    tournament_id INTEGER REFERENCES tournaments(tournament_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    total_pushups INTEGER DEFAULT 0,
    PRIMARY KEY (tournament_id, user_id)
);

-- Authentication tokens table (simple implementation)
CREATE TABLE auth_tokens (
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    token VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Simple log table
CREATE TABLE logs (
    log_id SERIAL PRIMARY KEY,
    tournament_id INTEGER REFERENCES tournaments(tournament_id) ON DELETE SET NULL,
    message TEXT NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Streaks table for unique feature
CREATE TABLE user_streaks (
    user_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_active DATE
);

-- Grant specific privileges to webserver user
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO webserver;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO webserver;