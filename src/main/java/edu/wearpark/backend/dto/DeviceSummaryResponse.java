package edu.wearpark.backend.dto;

import java.time.Instant;

public record DeviceSummaryResponse(
        String id,
        String deviceKey,
        Instant createdAt,
        Boolean isActive,
        Instant revokedAt
) {}
