-- Disable foreign key constraints to avoid dependency issues
SET session_replication_role = 'replica';

-- Truncate all tables (remove all data, keep table structure)
TRUNCATE TABLE auth_tokens CASCADE;
TRUNCATE TABLE logs CASCADE;
TRUNCATE TABLE tournament_participants CASCADE;
TRUNCATE TABLE tournaments CASCADE;
TRUNCATE TABLE pushup_records CASCADE;
TRUNCATE TABLE user_profiles CASCADE;
TRUNCATE TABLE user_streaks CASCADE;
TRUNCATE TABLE users CASCADE;

-- Reset auto-increment sequences for ID columns
ALTER SEQUENCE users_user_id_seq RESTART WITH 1;
ALTER SEQUENCE pushup_records_record_id_seq RESTART WITH 1;
ALTER SEQUENCE tournaments_tournament_id_seq RESTART WITH 1;
ALTER SEQUENCE logs_log_id_seq RESTART WITH 1;

-- Enable foreign key constraints
SET session_replication_role = 'origin';

SELECT 'Database reset complete!' AS message;