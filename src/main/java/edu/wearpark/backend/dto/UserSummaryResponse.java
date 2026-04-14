package edu.wearpark.backend.dto;

import edu.wearpark.backend.domain.UserPreferences;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.Instant;

@Builder
public record UserSummaryResponse(
   ObjectId id,
   String email,
   String firstName,
   String lastName,
   String role,
   Instant createdAt
) {
}
