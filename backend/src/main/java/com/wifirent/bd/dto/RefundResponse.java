package com.wifirent.bd.dto;

public record RefundResponse(
    Long refundId,
    Long rentalOrderId,
    Long amountMinorUnits,
    String status,
    String note
) {}
