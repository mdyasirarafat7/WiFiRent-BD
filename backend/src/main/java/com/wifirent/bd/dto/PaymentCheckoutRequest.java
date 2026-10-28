package com.wifirent.bd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCheckoutRequest(
    @NotNull Long rentalOrderId,
    @NotBlank String provider,
    String idempotencyKey
) {}
