package com.wifirent.bd.dto;

import java.time.Instant;

public record HostLedgerEntryDto(
    Long id,
    String entryType,
    Long amountMinorUnits,
    String currency,
    String referenceId,
    String referenceType,
    String description,
    Instant createdAt
) {}
