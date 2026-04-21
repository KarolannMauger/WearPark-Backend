package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.CreateDeviceRequest;
import edu.wearpark.backend.dto.UpdateDeviceRequest;
import edu.wearpark.backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DeviceUserController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<Device> createDevice(
            @RequestBody CreateDeviceRequest request,
            @AuthenticationPrincipal User user
    ) {
        Device device = deviceService.createDeviceForCurrentUser(
                request.deviceKey(),
                user.getId()
        );

        return ResponseEntity.ok(device);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Device> updateDevice(
            @PathVariable String id,
            @RequestBody UpdateDeviceRequest request,
            @AuthenticationPrincipal User user
    ) {
        Device device = deviceService.updateDeviceForUser(
                id,
                request.deviceKey()
        );

        return ResponseEntity.ok(device);
    }
}