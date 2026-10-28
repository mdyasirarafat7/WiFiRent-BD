package com.wifirent.bd.controller;

import com.wifirent.bd.domain.hotspot.GatewayDevice;
import com.wifirent.bd.dto.GatewayDeviceDto;
import com.wifirent.bd.dto.GatewayHealthDto;
import com.wifirent.bd.dto.GatewayRegisterRequest;
import com.wifirent.bd.dto.GatewayHeartbeatRequest;
import com.wifirent.bd.dto.GatewaySessionAuthorizeRequest;
import com.wifirent.bd.dto.GatewaySessionResponse;
import com.wifirent.bd.gateway.GatewayAdapter;
import com.wifirent.bd.service.GatewayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gateways")
@PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
public class GatewayController {

    private final GatewayService gatewayService;
    private final GatewayAdapter gatewayAdapter;

    public GatewayController(GatewayService gatewayService, GatewayAdapter gatewayAdapter) {
        this.gatewayService = gatewayService;
        this.gatewayAdapter = gatewayAdapter;
    }

    @PostMapping("/register")
    public ResponseEntity<GatewaySessionResponse> register(@Valid @RequestBody GatewayRegisterRequest request) {
        return ResponseEntity.ok(gatewayService.register(request));
    }

    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable String deviceId,
                                          @Valid @RequestBody GatewayHeartbeatRequest request) {
        gatewayService.heartbeat(deviceId, request.status());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deviceId}/events")
    public ResponseEntity<Void> events(@PathVariable String deviceId, @RequestBody String payload) {
        gatewayService.handleEvent(deviceId, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{deviceId}/sessions/{sessionId}/authorize")
    public ResponseEntity<GatewaySessionResponse> authorize(@PathVariable String deviceId,
                                                                    @PathVariable Long sessionId) {
        return ResponseEntity.ok(gatewayService.authorizeSession(deviceId, sessionId));
    }

    @PostMapping("/{deviceId}/sessions/{sessionId}/revoke")
    public ResponseEntity<Void> revoke(@PathVariable String deviceId, @PathVariable Long sessionId) {
        gatewayService.revokeSession(deviceId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GatewayDeviceDto>> listDevices() {
        List<GatewayDevice> devices = gatewayService.listDevices();
        List<GatewayDeviceDto> dtos = devices.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<GatewayDeviceDto> getDevice(@PathVariable String deviceId) {
        GatewayDevice device = gatewayService.getDevice(deviceId);
        return ResponseEntity.ok(toDto(device));
    }

    @GetMapping("/{deviceId}/health")
    public ResponseEntity<GatewayHealthDto> checkHealth(@PathVariable String deviceId) {
        GatewayAdapter.GatewayHealth health = gatewayService.checkHealth(deviceId);
        return ResponseEntity.ok(new GatewayHealthDto(
            health.online,
            health.lastHeartbeat != null ? health.lastHeartbeat.toString() : null
        ));
    }

    @PostMapping("/{deviceId}/test-connection")
    public ResponseEntity<Void> testConnection(@PathVariable String deviceId) {
        GatewayDevice device = gatewayService.getDevice(deviceId);
        // Attempt a health check to verify connectivity
        gatewayService.checkHealth(deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/capabilities")
    public ResponseEntity<Set<String>> getCapabilities() {
        return ResponseEntity.ok(gatewayAdapter.supportedCapabilities()
            .stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
    }

    private GatewayDeviceDto toDto(GatewayDevice device) {
        return new GatewayDeviceDto(
            device.getId(),
            device.getDeviceId(),
            device.getName(),
            device.getManagementUrl(),
            device.getAdapterType(),
            device.getIntegrationMode() != null ? device.getIntegrationMode().name() : "MOCK",
            device.getOnline() != null ? device.getOnline() : false,
            device.getLastHeartbeatAt() != null ? device.getLastHeartbeatAt().toString() : null,
            device.getStatus() != null ? device.getStatus().name() : "OFFLINE",
            device.getHotspot() != null ? device.getHotspot().getId() : null,
            device.getHotspot() != null ? device.getHotspot().getName() : null,
            device.getCapabilities() != null ?
                device.getCapabilities().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
                : java.util.Collections.emptySet(),
            device.getFirmwareVersion()
        );
    }
}
