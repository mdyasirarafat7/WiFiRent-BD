package com.wifirent.bd.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RefundRequest(
    @NotNull Long rentalOrderId,
    @NotNull @PositiveOrZero Long amountMinorUnits,
    String reason,
    String idempotencyKey
) {}
