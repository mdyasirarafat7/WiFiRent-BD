package com.wifirent.bd.dto;

public record PaymentWebhookRequest(
    String providerTransactionRef,
    String status,
    String idempotencyKey,
    String payload
) {}
