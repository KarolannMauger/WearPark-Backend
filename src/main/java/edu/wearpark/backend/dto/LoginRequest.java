package edu.wearpark.backend.dto;

public record LoginRequest(
        String email,
        String password
) {
}
