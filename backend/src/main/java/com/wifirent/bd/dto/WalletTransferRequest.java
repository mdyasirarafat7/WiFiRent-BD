package com.wifirent.bd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

public record WalletTransferRequest(
    @NotNull Long targetUserId,
    @NotNull @Positive Long amountMinorUnits,
    @NotBlank String referenceId,
    String idempotencyKey
) {}
