package com.wifirent.bd.dto;

public record GatewayRegisterRequest(
    String deviceId,
    String name,
    String managementUrl,
    String adapterType,
    Long hotspotId
) {}
