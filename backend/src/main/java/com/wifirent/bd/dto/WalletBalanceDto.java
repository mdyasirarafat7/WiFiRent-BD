package com.wifirent.bd.dto;

public record WalletBalanceDto(
    Long userId,
    Long balanceMinorUnits,
    String currency,
    String note
) {}
