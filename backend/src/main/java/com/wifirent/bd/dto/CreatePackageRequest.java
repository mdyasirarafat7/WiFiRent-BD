package com.wifirent.bd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreatePackageRequest(
    @NotNull @DecimalMin("0") Long priceMinorUnits,
    @NotNull String packageType,
    @PositiveOrZero Integer durationMinutes,
    @PositiveOrZero Long dataQuotaBytes,
    @PositiveOrZero Integer validityMinutes,
    @PositiveOrZero Integer speedLimitKbps
) {}
