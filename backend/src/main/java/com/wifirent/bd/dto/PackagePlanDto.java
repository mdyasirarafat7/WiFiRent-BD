package com.wifirent.bd.dto;

public record PackagePlanDto(
    Long id,
    Long priceMinorUnits,
    String packageType,
    Integer durationMinutes,
    Long dataQuotaBytes,
    Integer validityMinutes,
    Integer speedLimitKbps,
    Boolean active
) {}
