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
@Document(collection = "motion_entries")
public class MotionEntry {
    @MongoId
    private ObjectId id;
    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;
    @Indexed
    private Instant start;
    private Instant end;
    @Field(name = "nb_entries")
    private Integer nbEntries;
    private byte[] data;
}
