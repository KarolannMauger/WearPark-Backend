package edu.wearpark.backend.dto;

import lombok.Builder;

@Builder
public record UpdateDeviceAdminRequest(
        String deviceKey,
        Boolean isActive
) { }