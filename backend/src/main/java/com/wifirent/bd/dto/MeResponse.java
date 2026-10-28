package com.wifirent.bd.dto;

public record MeResponse(
    Long id, String name, String email, String role,
    Boolean enabled, Boolean emailVerified
) {}
