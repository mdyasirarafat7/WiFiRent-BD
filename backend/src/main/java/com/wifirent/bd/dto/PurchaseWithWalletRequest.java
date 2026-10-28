package com.wifirent.bd.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseWithWalletRequest(
    @NotNull Long hotspotId,
    @NotNull Long packageId,
    String idempotencyKey
) {}
