package com.wifirent.bd.dto;

public record UpdatePackageRequest(
    Long priceMinorUnits,
    String packageType,
    Integer durationMinutes,
    Long dataQuotaBytes,
    Integer validityMinutes,
    Integer speedLimitKbps,
    Boolean active
) {}
