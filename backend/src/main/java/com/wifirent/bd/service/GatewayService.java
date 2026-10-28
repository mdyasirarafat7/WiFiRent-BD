package com.wifirent.bd.service;

import com.wifirent.bd.domain.hotspot.GatewayCapability;
import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.domain.hotspot.GatewayIntegrationMode;
import com.wifirent.bd.domain.hotspot.repository.GatewayDeviceRepository;
import com.wifirent.bd.domain.session.RentalSession;
import com.wifirent.bd.domain.session.SessionStatus;
import com.wifirent.bd.domain.session.repository.RentalSessionRepository;

import com.wifirent.bd.gateway.GatewayAdapter;
import com.wifirent.bd.exception.BadRequestException;
import com.wifirent.bd.exception.ForbiddenException;
import com.wifirent.bd.exception.ResourceNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wifirent.bd.dto.*;

@Service
public class GatewayService {

    private final GatewayDeviceRepository deviceRepository;
    private final RentalSessionRepository sessionRepository;
    private final GatewayAdapter gatewayAdapter;

    public GatewayService(GatewayDeviceRepository deviceRepository,
                          RentalSessionRepository sessionRepository,
                          GatewayAdapter gatewayAdapter) {
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.gatewayAdapter = gatewayAdapter;
    }

    @Transactional
    public GatewaySessionResponse register(GatewayRegisterRequest request) {
        // Validate management URL for SSRF protection
        validateManagementUrl(request.managementUrl());

        GatewayDevice device = new GatewayDevice();
        device.setDeviceId(request.deviceId());
        device.setName(request.name());
        device.setManagementUrl(request.managementUrl());
        device.setAdapterType(request.adapterType() != null ? request.adapterType() : "MOCK");
        device.setOnline(false);
        device.setStatus(GatewayDevice.GatewayStatus.OFFLINE);
        device.setIntegrationMode(
            "MOCK".equalsIgnoreCase(request.adapterType())
                ? GatewayIntegrationMode.MOCK
                : GatewayIntegrationMode.REAL);

        if (request.hotspotId() != null) {
            device.setHotspot(new com.wifirent.bd.domain.hotspot.Hotspot());
            device.getHotspot().setId(request.hotspotId());
        }

        GatewayDevice saved = deviceRepository.save(device);
        gatewayAdapter.registerGateway(saved);

        return new GatewaySessionResponse(
            saved.getDeviceId(), null, saved.getStatus().name(), null);
    }

    @Transactional
    public void heartbeat(String deviceId, String status) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        gatewayAdapter.heartbeat(device, status, Instant.now());
        device.setLastHeartbeatAt(Instant.now());
        device.setOnline("online".equalsIgnoreCase(status));
        device.setStatus("online".equalsIgnoreCase(status)
            ? GatewayDevice.GatewayStatus.ONLINE : GatewayDevice.GatewayStatus.OFFLINE);
        deviceRepository.save(device);
    }

    @Transactional
    public void handleEvent(String deviceId, String payload) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        device.setLastHeartbeatAt(Instant.now());
        deviceRepository.save(device);
    }

    @Transactional
    public GatewaySessionResponse authorizeSession(String deviceId, Long sessionId) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        RentalSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Verify gateway supports session authorization
        if (!device.getCapabilities().contains(GatewayCapability.SESSION_REVOCATION)) {
            throw new BadRequestException(
                "Gateway does not support session authorization");
        }

        GatewayAdapter.GatewaySessionResult result = gatewayAdapter.authorizeSession(
            device, session.getGatewaySessionId(),
            session.getPackagePlan().getDurationMinutes(),
            session.getDataQuotaBytes(),
            session.getPackagePlan().getSpeedLimitKbps());

        if (!result.authorized) {
            throw new BadRequestException("Authorization failed: " + result.error);
        }

        session.setGatewaySessionId(result.gatewaySessionId);
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.save(session);

        return new GatewaySessionResponse(
            device.getDeviceId(), session.getId(), session.getStatus().name(),
            session.getGatewaySessionId());
    }

    @Transactional
    public void revokeSession(String deviceId, Long sessionId) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        RentalSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        gatewayAdapter.revokeSession(device, session.getGatewaySessionId());
        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);
    }

    public List<GatewayDevice> listDevices() {
        return deviceRepository.findAll();
    }

    public GatewayDevice getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
    }

    public List<GatewayDevice> getDevicesByHost(Long hostId) {
        return deviceRepository.findByHotspotHostUserId(hostId);
    }

    public GatewayAdapter.GatewayHealth checkHealth(String deviceId) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        return gatewayAdapter.checkHealth(device);
    }

    public void validateCapabilities(String deviceId, Set<GatewayCapability> requiredCapabilities) {
        GatewayDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));
        Set<GatewayCapability> deviceCapabilities = device.getCapabilities();
        if (deviceCapabilities == null) {
            throw new BadRequestException("Gateway has no capabilities configured");
        }
        for (GatewayCapability cap : requiredCapabilities) {
            if (!deviceCapabilities.contains(cap)) {
                throw new BadRequestException(
                    "Gateway does not support capability: " + cap);
            }
        }
    }

    /**
     * SSRF Protection: Validate that the management URL is not pointing to
     * localhost, internal networks, or cloud metadata endpoints.
     */
    private void validateManagementUrl(String url) {
        if (url == null || url.isBlank()) {
            return; // No management URL is acceptable for mock gateways
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                throw new BadRequestException("Invalid management URL: missing host");
            }

            // Block localhost and loopback
            if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                || "0.0.0.0".equals(host) || host.startsWith("127.")) {
                throw new BadRequestException("Invalid management URL: localhost access not allowed");
            }

            // Block private IP ranges (RFC 1918)
            if (host.startsWith("10.") || host.startsWith("172.16.")
                || host.startsWith("172.17.") || host.startsWith("172.18.")
                || host.startsWith("172.19.") || host.startsWith("172.20.")
                || host.startsWith("172.21.") || host.startsWith("172.22.")
                || host.startsWith("172.23.") || host.startsWith("172.24.")
                || host.startsWith("172.25.") || host.startsWith("172.26.")
                || host.startsWith("172.27.") || host.startsWith("172.28.")
                || host.startsWith("172.29.") || host.startsWith("172.30.")
                || host.startsWith("172.31.") || host.startsWith("192.168.")) {
                throw new BadRequestException("Invalid management URL: private IP not allowed");
            }

            // Block cloud metadata endpoints
            if (host.contains("169.254.169.254")) {
                throw new BadRequestException("Invalid management URL: metadata endpoint not allowed");
            }

            // Only allow HTTP/HTTPS
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new BadRequestException("Invalid management URL: only HTTP/HTTPS allowed");
            }
        } catch (URISyntaxException e) {
            throw new BadRequestException("Invalid management URL: " + e.getMessage());
        }
    }
}
