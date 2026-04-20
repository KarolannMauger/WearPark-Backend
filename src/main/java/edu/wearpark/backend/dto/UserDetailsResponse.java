package edu.wearpark.backend.dto;

import edu.wearpark.backend.domain.UserPreferences;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UserDetailsResponse(
   String email,
   String gender,
   String firstName,
   String lastName,
   String role,
   Instant dateOfBirth,
   Boolean hasDiagnosis,
   String diagnosis,
   UserPreferences userPreferences
) {
}
