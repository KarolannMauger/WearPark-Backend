package edu.wearpark.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MotionData {
    private byte[] ax;
    private byte[] ay;
    private byte[] az;
    private byte[] gx;
    private byte[] gy;
    private byte[] gz;
}
