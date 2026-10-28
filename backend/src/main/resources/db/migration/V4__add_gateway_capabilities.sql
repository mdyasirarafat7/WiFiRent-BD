-- V4__add_gateway_capabilities.sql
-- Add gateway capability tracking and firmware version for OpenWrt integration

ALTER TABLE gateway_devices ADD COLUMN capabilities VARCHAR(500);
ALTER TABLE gateway_devices ADD COLUMN firmware_version VARCHAR(100);
ALTER TABLE gateway_devices ADD COLUMN integration_mode VARCHAR(20) NOT NULL DEFAULT 'MOCK';

CREATE INDEX idx_gateway_devices_adapter_type ON gateway_devices(adapter_type);
CREATE INDEX idx_gateway_devices_integration_mode ON gateway_devices(integration_mode);
