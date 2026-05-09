package edu.wearpark.backend.domain.view;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MotionGraph {
    private Instant start;
    private Instant end;
    private Float max;
    private Float min;
    private byte[] data;
}
