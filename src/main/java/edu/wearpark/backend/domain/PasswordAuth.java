package edu.wearpark.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordAuth {
    @Field(name = "pwd_digest")
    private String hash;
    @Field(name = "attempts")
    private Integer attempts;
    @Field(name = "last_attempts")
    private Instant lastAttempt;
}
