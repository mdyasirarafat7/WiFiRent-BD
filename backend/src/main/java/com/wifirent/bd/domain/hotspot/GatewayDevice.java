package com.wifirent.bd.domain.hotspot;

import com.wifirent.bd.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "gateway_devices")
public class GatewayDevice extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "device_id", nullable = false, unique = true, length = 100)
    private String deviceId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "management_url", length = 500)
    private String managementUrl;

    @Size(max = 255)
    @Column(name = "adapter_type", length = 255)
    private String adapterType = "MOCK";

    @Column(name = "is_online", nullable = false)
    private Boolean online = false;

    @Column(name = "last_heartbeat_at")
    private java.time.Instant lastHeartbeatAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GatewayStatus status = GatewayStatus.OFFLINE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotspot_id", nullable = false)
    private Hotspot hotspot;

    @Column(name = "shared_secret", length = 500)
    private String sharedSecret;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", length = 50)
    private Set<GatewayCapability> capabilities;

    @Column(name = "firmware_version", length = 100)
    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_mode", nullable = false, length = 20)
    private GatewayIntegrationMode integrationMode = GatewayIntegrationMode.MOCK;

    public GatewayDevice() {
    }

    public enum GatewayStatus {
        ONLINE, OFFLINE, UNKNOWN
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagementUrl() {
        return managementUrl;
    }

    public void setManagementUrl(String managementUrl) {
        this.managementUrl = managementUrl;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public java.time.Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(java.time.Instant lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public GatewayStatus getStatus() {
        return status;
    }

    public void setStatus(GatewayStatus status) {
        this.status = status;
    }

    public Hotspot getHotspot() {
        return hotspot;
    }

    public void setHotspot(Hotspot hotspot) {
        this.hotspot = hotspot;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public Set<GatewayCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<GatewayCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public GatewayIntegrationMode getIntegrationMode() {
        return integrationMode;
    }

    public void setIntegrationMode(GatewayIntegrationMode integrationMode) {
        this.integrationMode = integrationMode;
    }
}
