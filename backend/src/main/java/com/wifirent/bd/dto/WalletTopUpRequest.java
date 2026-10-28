package com.wifirent.bd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WalletTopUpRequest(
    @NotNull Long targetUserId,
    @NotNull @DecimalMin("0") Long amountMinorUnits,
    @NotBlank String referenceId,
    String idempotencyKey
) {}
