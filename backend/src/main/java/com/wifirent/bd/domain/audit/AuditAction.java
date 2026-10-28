package com.wifirent.bd.domain.audit;

public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    LOGIN,
    LOGOUT,
    AUTHORIZE_SESSION,
    REVOKE_SESSION,
    PAYMENT,
    REFUND,
    LEDGER_ENTRY,
    HOTSPOT_STATUS_CHANGE,
    PACKAGE_CHANGE,
    WALLET_TOPUP,
    WALLET_TRANSFER,
    WALLET_PURCHASE
}
