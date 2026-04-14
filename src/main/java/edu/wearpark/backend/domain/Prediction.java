package edu.wearpark.backend.domain;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@Document(collection = "predictions")
public class Prediction {
    @MongoId
    private ObjectId id;
    @Indexed
    @Field(name = "motion_entry_id")
    private ObjectId motionEntryId;
    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;
    private String state;
    private String label;
    private Double probability;
    private String confidence;
    private Integer prediction;
    @Indexed
    @Field(name = "created_at")
    private Instant createdAt;
}
