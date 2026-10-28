package com.wifirent.bd.dto;

import jakarta.validation.constraints.Size;

public record UpdateHotspotRequest(
    @Size(max = 200) String name,
    @Size(max = 1000) String description,
    @Size(max = 100) String ssid,
    @Size(max = 100) String approximateArea
) {}
