package com.wifirent.bd.gateway;

import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.gateway.GatewayAdapter.GatewayHealth;
import com.wifirent.bd.gateway.GatewayAdapter.GatewaySessionResult;
import com.wifirent.bd.gateway.GatewayAdapter.GatewaySessionStatus;
import com.wifirent.bd.gateway.GatewayAdapter.GatewayUsage;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MockGatewayAdapterTest {

    @Test
    void registerGateway_shouldMarkOnline() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        GatewayDevice device = new GatewayDevice();
        device.setDeviceId("dev-1");
        device.setName("Test Gateway");

        adapter.registerGateway(device);

        GatewayHealth health = adapter.checkHealth(device);
        assertTrue(health.online);
        assertNotNull(health.lastHeartbeat);
    }

    @Test
    void heartbeat_shouldUpdateStatus() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        GatewayDevice device = new GatewayDevice();
        device.setDeviceId("dev-1");

        adapter.heartbeat(device, "online", Instant.now());

        GatewayHealth health = adapter.checkHealth(device);
        assertTrue(health.online);
    }

    @Test
    void authorizeSession_shouldReturnAuthorized() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        GatewayDevice device = new GatewayDevice();
        device.setDeviceId("dev-1");

        GatewaySessionResult result = adapter.authorizeSession(
            device, "sess-1", 60, 1073741824L, 1000);

        assertTrue(result.authorized);
        assertEquals("sess-1", result.gatewaySessionId);
        assertNull(result.error);
    }

    @Test
    void revokeSession_shouldMarkInactive() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        GatewayDevice device = new GatewayDevice();
        device.setDeviceId("dev-1");

        adapter.authorizeSession(device, "sess-1", 60, 1073741824L, 1000);
        adapter.revokeSession(device, "sess-1");

        GatewaySessionStatus status = adapter.getSessionStatus(device, "sess-1");
        assertFalse(status.active);
    }

    @Test
    void getUsage_shouldReturnZeroWhenNoSession() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        GatewayDevice device = new GatewayDevice();
        device.setDeviceId("dev-1");

        GatewayUsage usage = adapter.getUsage(device, "nonexistent");
        assertEquals(0L, usage.consumedBytes);
        assertEquals(0L, usage.durationSeconds);
    }

    @Test
    void adapterType_shouldReturnMock() {
        MockGatewayAdapter adapter = new MockGatewayAdapter();
        assertEquals("MOCK", adapter.adapterType());
    }
}
