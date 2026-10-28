package com.wifirent.bd.dto;

public record PaymentDto(
    Long id,
    Long rentalOrderId,
    String provider,
    String status,
    Long amountMinorUnits,
    String currency,
    String providerTransactionRef
) {}
