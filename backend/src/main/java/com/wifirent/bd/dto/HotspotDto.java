package com.wifirent.bd.dto;

import java.math.BigDecimal;

public record HotspotDto(
    Long id,
    String name,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    String ssid,
    String approximateArea,
    Boolean available,
    String status,
    Long hostId,
    String hostBusinessName
) {}
