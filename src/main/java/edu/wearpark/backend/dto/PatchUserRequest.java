package edu.wearpark.backend.dto;

import edu.wearpark.backend.domain.UserPreferences;

import java.time.Instant;

public record PatchUserRequest(
        String gender,
        String firstName,
        String lastName,
        Instant dateOfBirth,
        Boolean hasDiagnosis,
        String diagnosis,
        UserPreferences userPreferences
) {}
