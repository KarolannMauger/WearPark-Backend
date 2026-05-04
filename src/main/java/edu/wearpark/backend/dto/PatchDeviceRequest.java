package edu.wearpark.backend.dto;

import lombok.Builder;

@Builder
public record PatchDeviceRequest(
    String deviceKey
) {}