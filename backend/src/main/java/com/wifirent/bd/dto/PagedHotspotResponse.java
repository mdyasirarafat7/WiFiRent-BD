package com.wifirent.bd.dto;

import java.util.List;

public record PagedHotspotResponse(
    List<HotspotDto> content,
    Integer page, Integer size, Long totalElements, Integer totalPages
) {}
