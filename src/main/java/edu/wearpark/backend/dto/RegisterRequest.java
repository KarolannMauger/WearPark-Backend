package edu.wearpark.backend.dto;

public record RegisterRequest(
        String email,
        String password
) {}
