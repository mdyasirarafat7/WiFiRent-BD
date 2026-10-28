package com.wifirent.bd.dto;

import java.time.Instant;

public record ActiveSessionResponse(
    boolean hasActiveSession,
    SessionDto session
) {}
