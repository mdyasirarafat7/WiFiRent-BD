package com.wifirent.bd.dto;

public record GatewaySessionResponse(
    String deviceId,
    Long sessionId,
    String status,
    String gatewaySessionId
) {}
