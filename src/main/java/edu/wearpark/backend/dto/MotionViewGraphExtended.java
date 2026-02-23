package edu.wearpark.backend.dto;

import lombok.*;

import java.time.Instant;

@Builder
public record MotionViewGraphExtended(
        Instant date,
        Float avgIntensity,
        Integer avgDurationMs,
        Integer nbEpisode,
        Instant lastEpisode,
        MotionViewGraph graph
) {}
