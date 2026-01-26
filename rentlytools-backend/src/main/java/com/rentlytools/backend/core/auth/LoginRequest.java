package com.rentlytools.backend.core.auth;

public record LoginRequest(
        String email,
        String password
) {}
