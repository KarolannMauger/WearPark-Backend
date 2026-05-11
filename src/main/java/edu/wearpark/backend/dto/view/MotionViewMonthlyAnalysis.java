package edu.wearpark.backend.dto.view;

import lombok.Builder;

import java.time.Instant;
import java.util.List;


@Builder
public record MotionViewMonthlyAnalysis(
        Instant start,
        Instant end,

        Double coverage,
        Double meanAmplitude,
        List<MotionViewDailySummary> days
) {}
