-- Example Flyway migration: create notifications table
CREATE TABLE IF NOT EXISTS file_share_notification (
    id BIGSERIAL PRIMARY KEY,
    sender_id VARCHAR(255),
    recipient_id VARCHAR(255),
    file_id VARCHAR(255),
    message TEXT,
    timestamp VARCHAR(255),
    read BOOLEAN
); 