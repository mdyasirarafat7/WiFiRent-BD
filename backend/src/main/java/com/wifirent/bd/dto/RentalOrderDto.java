package com.wifirent.bd.dto;

public record RentalOrderDto(
    Long id,
    Long customerId,
    Long hotspotId,
    Long packageId,
    String status,
    Long totalPriceMinorUnits,
    String currency
) {}
