package edu.wearpark.backend.dto;

import java.time.Instant;
import java.util.List;

public record AdminUserDetailsResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant dateOfBirth,
        String gender,
        Boolean hasDiagnosis,
        String diagnosis,
        Instant createdAt,
        Instant updatedAt,
        Boolean isDeleted,
        List<DeviceAdminResponse> devices
) {
}
