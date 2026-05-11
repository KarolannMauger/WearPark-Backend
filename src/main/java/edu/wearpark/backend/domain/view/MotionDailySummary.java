package edu.wearpark.backend.domain.view;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Builder
public class MotionDailySummary {
    private Instant start;
    private Instant end;
    private Double coverage;
    @Field(name = "mean_amplitude")
    private Double meanAmplitude;
    @Field(name = "peak_amplitude")
    private Double peakAmplitude;
    private Double variance;
    @Field(name = "delta_mean_amplitude")
    private Double deltaMeanAmplitude;
}
