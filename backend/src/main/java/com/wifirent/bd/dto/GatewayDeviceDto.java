package com.wifirent.bd.dto;

import com.wifirent.bd.domain.hotspot.GatewayCapability;
import java.util.Set;

public record GatewayDeviceDto(
    Long id,
    String deviceId,
    String name,
    String managementUrl,
    String adapterType,
    String integrationMode,
    Boolean online,
    String lastHeartbeatAt,
    String status,
    Long hotspotId,
    String hotspotName,
    Set<String> capabilities,
    String firmwareVersion
) {}
