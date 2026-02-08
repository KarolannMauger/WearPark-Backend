package edu.wearpark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotionEntryResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MotionDataResponse {
        String ax;
        String ay;
        String az;
        String gx;
        String gy;
        String gz;
    }
    String id;
    Instant start;
    Instant end;
    MotionDataResponse data;


}
