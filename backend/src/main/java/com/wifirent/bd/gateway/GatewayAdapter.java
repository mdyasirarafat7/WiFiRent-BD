package com.wifirent.bd.gateway;

import com.wifirent.bd.domain.hotspot.GatewayCapability;
import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.domain.hotspot.GatewayIntegrationMode;
import java.time.Instant;
import java.util.Set;

public interface GatewayAdapter {

    String adapterType();

    GatewayIntegrationMode integrationMode();

    Set<GatewayCapability> supportedCapabilities();

    void registerGateway(GatewayDevice device);

    GatewayHealth checkHealth(GatewayDevice device);

    void heartbeat(GatewayDevice device, String status, Instant now);

    GatewaySessionResult authorizeSession(GatewayDevice device, String gatewaySessionId,
                                          Integer durationMinutes, Long dataQuotaBytes,
                                          Integer speedLimitKbps);

    void revokeSession(GatewayDevice device, String gatewaySessionId);

    GatewaySessionStatus getSessionStatus(GatewayDevice device, String gatewaySessionId);

    GatewayUsage getUsage(GatewayDevice device, String gatewaySessionId);

    void updatePackageLimits(GatewayDevice device, String gatewaySessionId,
                             Integer durationMinutes, Long dataQuotaBytes);

    class GatewayHealth {
        public final boolean online;
        public final Instant lastHeartbeat;
        public GatewayHealth(boolean online, Instant lastHeartbeat) {
            this.online = online;
            this.lastHeartbeat = lastHeartbeat;
        }
    }

    class GatewaySessionResult {
        public final boolean authorized;
        public final String gatewaySessionId;
        public final String error;
        public GatewaySessionResult(boolean authorized, String gatewaySessionId, String error) {
            this.authorized = authorized;
            this.gatewaySessionId = gatewaySessionId;
            this.error = error;
        }
    }

    class GatewaySessionStatus {
        public final String status;
        public final Boolean active;
        public GatewaySessionStatus(String status, Boolean active) {
            this.status = status;
            this.active = active;
        }
    }

    class GatewayUsage {
        public final Long consumedBytes;
        public final Long durationSeconds;
        public GatewayUsage(Long consumedBytes, Long durationSeconds) {
            this.consumedBytes = consumedBytes;
            this.durationSeconds = durationSeconds;
        }
    }
}
