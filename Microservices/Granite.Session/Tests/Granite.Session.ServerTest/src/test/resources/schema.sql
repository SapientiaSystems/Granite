CREATE INDEX IF NOT EXISTS ix_session_user_id ON Session (userId);
CREATE INDEX IF NOT EXISTS ix_session_user_id_device_id ON Session (userId, deviceId);
