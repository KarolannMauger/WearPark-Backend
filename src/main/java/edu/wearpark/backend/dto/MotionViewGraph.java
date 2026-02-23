package edu.wearpark.backend.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MotionViewGraph(
    Instant start,
    Instant end,
    Float max,
    Float min,
    byte[] data
){}
