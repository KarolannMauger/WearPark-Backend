package edu.wearpark.backend.dto.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
public record MotionViewDailySummary (
        Instant start,
        Instant end,
        Double coverage,
        Double meanAmplitude,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Double deltaMeanAmplitude
) {}
