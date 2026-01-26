package edu.wearpark.backend.domain;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

public class UserAuth {
    @Field(name = "pwd_digest")
    private String hash;
    @Field(name = "attempts")
    private Integer attempts;
    @Field(name = "last_attempts")
    private Instant lastAttempts;
}
