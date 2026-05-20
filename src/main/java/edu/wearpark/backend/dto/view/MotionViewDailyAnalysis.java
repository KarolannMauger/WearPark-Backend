package edu.wearpark.backend.dto.view;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MotionViewDailyAnalysis(
        Instant start,
        Instant end,
        Double coverage,
        Double meanAmplitude,
        Double peakAmplitude,
        Double variance,
        MotionViewGraph graph
) {}
