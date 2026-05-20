package edu.wearpark.backend.dto;

import edu.wearpark.backend.domain.Prediction;

import java.time.Instant;

public record PredictionResponse(
        String  id,
        String  motionEntryId,
        String  state,
        String  label,
        double  probability,
        String  confidence,
        int     prediction,
        Instant createdAt
) {
    public static PredictionResponse from(Prediction p) {
        return new PredictionResponse(
                p.getId().toHexString(),
                p.getMotionEntryId().toHexString(),
                p.getState(),
                p.getLabel(),
                p.getProbability(),
                p.getConfidence(),
                p.getPrediction(),
                p.getCreatedAt()
        );
    }
}
