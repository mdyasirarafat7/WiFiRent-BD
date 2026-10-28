package com.wifirent.bd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateRentalOrderRequest(
    @NotNull Long hotspotId,
    @NotNull Long packageId,
    String idempotencyKey
) {}
