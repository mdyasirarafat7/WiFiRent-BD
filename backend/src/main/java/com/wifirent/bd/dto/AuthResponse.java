package com.wifirent.bd.dto;

public record AuthResponse(
    Long id, String name, String email, String role,
    String accessToken, String refreshToken
) {}
