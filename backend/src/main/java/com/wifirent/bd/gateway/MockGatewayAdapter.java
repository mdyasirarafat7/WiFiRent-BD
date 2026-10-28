package com.wifirent.bd.gateway;

import com.wifirent.bd.domain.hotspot.GatewayCapability;
import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.domain.hotspot.GatewayIntegrationMode;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MockGatewayAdapter implements GatewayAdapter {

    private final Map<String, MockSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Instant> heartbeats = new ConcurrentHashMap<>();
    private final Map<String, Boolean> online = new ConcurrentHashMap<>();

    @Override
    public String adapterType() {
        return "MOCK";
    }

    @Override
    public GatewayIntegrationMode integrationMode() {
        return GatewayIntegrationMode.MOCK;
    }

    @Override
    public Set<GatewayCapability> supportedCapabilities() {
        return Set.of(
            GatewayCapability.TIME_BASED_ACCESS,
            GatewayCapability.DATA_BASED_ACCESS,
            GatewayCapability.CAPTIVE_PORTAL,
            GatewayCapability.SESSION_REVOCATION,
            GatewayCapability.USAGE_REPORTING,
            GatewayCapability.SPEED_LIMITING,
            GatewayCapability.DEVICE_ISOLATION,
            GatewayCapability.HEARTBEAT_MONITORING
        );
    }

    @Override
    public void registerGateway(GatewayDevice device) {
        online.put(device.getDeviceId(), true);
        heartbeats.put(device.getDeviceId(), Instant.now());
    }

    @Override
    public GatewayHealth checkHealth(GatewayDevice device) {
        Instant last = heartbeats.get(device.getDeviceId());
        Boolean isOnline = online.getOrDefault(device.getDeviceId(), false);
        return new GatewayHealth(isOnline, last);
    }

    @Override
    public void heartbeat(GatewayDevice device, String status, Instant now) {
        heartbeats.put(device.getDeviceId(), now);
        online.put(device.getDeviceId(), "online".equalsIgnoreCase(status));
    }

    @Override
    public GatewaySessionResult authorizeSession(GatewayDevice device, String gatewaySessionId,
                                                 Integer durationMinutes, Long dataQuotaBytes,
                                                 Integer speedLimitKbps) {
        sessions.put(gatewaySessionId, new MockSession(gatewaySessionId,
            durationMinutes, dataQuotaBytes, speedLimitKbps, Instant.now(), true));
        return new GatewaySessionResult(true, gatewaySessionId, null);
    }

    @Override
    public void revokeSession(GatewayDevice device, String gatewaySessionId) {
        MockSession s = sessions.get(gatewaySessionId);
        if (s != null) {
            s.active = false;
        }
    }

    @Override
    public GatewaySessionStatus getSessionStatus(GatewayDevice device, String gatewaySessionId) {
        MockSession s = sessions.get(gatewaySessionId);
        if (s == null) {
            return new GatewaySessionStatus("unknown", false);
        }
        return new GatewaySessionStatus(s.active ? "active" : "revoked", s.active);
    }

    @Override
    public GatewayUsage getUsage(GatewayDevice device, String gatewaySessionId) {
        MockSession s = sessions.get(gatewaySessionId);
        if (s == null) {
            return new GatewayUsage(0L, 0L);
        }
        return new GatewayUsage(s.consumedBytes, s.durationSeconds);
    }

    @Override
    public void updatePackageLimits(GatewayDevice device, String gatewaySessionId,
                                    Integer durationMinutes, Long dataQuotaBytes) {
        MockSession s = sessions.get(gatewaySessionId);
        if (s != null) {
            s.durationMinutes = durationMinutes;
            s.dataQuotaBytes = dataQuotaBytes;
        }
    }

    static class MockSession {
        String gatewaySessionId;
        Integer durationMinutes;
        Long dataQuotaBytes;
        Integer speedLimitKbps;
        Instant startedAt;
        Boolean active;
        Long consumedBytes = 0L;
        Long durationSeconds = 0L;

        MockSession(String gatewaySessionId, Integer durationMinutes, Long dataQuotaBytes,
                    Integer speedLimitKbps, Instant startedAt, Boolean active) {
            this.gatewaySessionId = gatewaySessionId;
            this.durationMinutes = durationMinutes;
            this.dataQuotaBytes = dataQuotaBytes;
            this.speedLimitKbps = speedLimitKbps;
            this.startedAt = startedAt;
            this.active = active;
        }
    }
}
