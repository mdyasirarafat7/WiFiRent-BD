-- V2__add_token_version.sql
-- Add token_version column for refresh token revocation support

ALTER TABLE users ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
