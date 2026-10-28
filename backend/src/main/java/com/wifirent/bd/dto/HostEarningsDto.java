package com.wifirent.bd.dto;

public record HostEarningsDto(
    Long hostId,
    Long grossRevenueMinorUnits,
    Long platformCommissionMinorUnits,
    Long hostEarningsMinorUnits,
    String currency
) {}
