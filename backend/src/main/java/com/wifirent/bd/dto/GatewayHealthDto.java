package com.wifirent.bd.dto;

public record GatewayHealthDto(
    Boolean online,
    String lastHeartbeatAt
) {}
