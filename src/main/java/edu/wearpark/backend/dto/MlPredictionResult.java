package edu.wearpark.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MlPredictionResult(
        int    prediction,
        Double probability,
        String state,
        String label,
        String confidence
) {}
