package edu.wearpark.backend.dto;

import java.time.Instant;

public record DeviceAdminResponse(
        String id,
        String deviceKey,
        Boolean isActive,
        Instant createdAt,
        Instant revokedAt
) {}
