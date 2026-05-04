package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.CreateDeviceRequest;
import edu.wearpark.backend.dto.DeviceUserResponse;
import edu.wearpark.backend.dto.PatchDeviceRequest;
import edu.wearpark.backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DeviceUserController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceUserResponse>> getAllDevices(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.getDevicesForUser(user.getId()));
    }

    @PostMapping
    public ResponseEntity<DeviceUserResponse> createDevice(@RequestBody CreateDeviceRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(deviceService.createDeviceForUser(request.deviceKey(), user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceUserResponse> updateDevice(@PathVariable String id, @RequestBody PatchDeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDeviceForUser(id, request.deviceKey()));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableDevice(@PathVariable String id) {
        deviceService.disableDevice(id);
        return ResponseEntity.noContent().build();
    }
}