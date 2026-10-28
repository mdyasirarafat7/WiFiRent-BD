package com.wifirent.bd.dto;

import java.time.Instant;

public record SessionDto(
    Long id,
    Long customerId,
    Long hotspotId,
    Long packageId,
    Long rentalOrderId,
    String status,
    Instant startedAt,
    Instant expiresAt,
    Long dataQuotaBytes,
    Long consumedBytes,
    String gatewaySessionId
) {}
