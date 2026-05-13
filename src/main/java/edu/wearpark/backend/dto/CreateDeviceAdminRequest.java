package edu.wearpark.backend.dto;

public record CreateDeviceAdminRequest(
        String userId,
        String deviceKey
) {}