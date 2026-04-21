package edu.wearpark.backend.dto;

import java.time.Instant;
import java.util.List;

public record AdminUserDetailsResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant createdAt,
        List<DeviceSummaryResponse> devices
) {
}
