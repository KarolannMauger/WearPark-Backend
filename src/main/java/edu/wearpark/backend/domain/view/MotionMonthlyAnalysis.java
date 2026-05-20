package edu.wearpark.backend.domain.view;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document(collection = "motion_monthly_analysis")
public class MotionMonthlyAnalysis {
    @MongoId
    private ObjectId id;
    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;
    @Indexed
    private Instant start;
    private Instant end;
    private Double coverage;
    @Field(name = "mean_amplitude")
    private Double meanAmplitude;
    private List<MotionDailySummary> days;
}
