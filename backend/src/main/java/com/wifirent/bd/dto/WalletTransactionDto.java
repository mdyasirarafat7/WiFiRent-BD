package com.wifirent.bd.dto;

import java.time.Instant;

public record WalletTransactionDto(
    Long id,
    String transactionType,
    Long amountMinorUnits,
    String referenceId,
    String referenceType,
    String description,
    String idempotencyKey,
    Instant createdAt
) {}
