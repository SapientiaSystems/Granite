CREATE INDEX IF NOT EXISTS ix_session_user_id ON Session (userId);
CREATE INDEX IF NOT EXISTS ix_session_user_id_device_id ON Session (userId, deviceId);
CREATE INDEX IF NOT EXISTS ix_session_previous_refresh_token_hash ON Session (previousRefreshTokenHash);
CREATE UNIQUE INDEX IF NOT EXISTS ux_session_active_user_device ON Session (userId, deviceId, activeMarker);
