package com.wifirent.bd.dto;

public record PurchaseWithWalletResponse(
    Long orderId,
    Long customerDebitedMinorUnits,
    Long hostCreditedMinorUnits,
    Long platformCommissionMinorUnits,
    Long customerRemainingBalanceMinorUnits,
    String status,
    String note
) {}
