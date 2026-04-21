package edu.wearpark.backend.domain;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@Document(collection = "devices")
public class Device {
    @MongoId
    private ObjectId id;
    @Indexed
    @Field(name = "device_key")
    private String deviceKey;
    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;

    // audit
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    @Field("is_active")
    private Boolean isActive = true;
    @Field("revoked_at")
    private Instant revokedAt;
}
