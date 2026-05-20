package edu.wearpark.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reports")
public class Report {
    @MongoId
    private ObjectId id;

    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;

    @Field(name = "year")
    private int year;

    @Field(name = "month")
    private int month;

    @Field(name = "title")
    private String title;

    @Indexed
    @Field(name = "generated_at")
    private Instant generatedAt;
}
