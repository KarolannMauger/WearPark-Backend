package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.dto.CreateDeviceAdminRequest;
import edu.wearpark.backend.dto.DeviceAdminResponse;
import edu.wearpark.backend.dto.PatchDeviceRequest;
import edu.wearpark.backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/devices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DeviceAdminController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<DeviceAdminResponse> createDevice(@RequestBody CreateDeviceAdminRequest request) {
        return ResponseEntity.ok(deviceService.createDeviceForAdmin(request.userId(), request.deviceKey()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DeviceAdminResponse> updateDevice(@PathVariable String id, @RequestBody PatchDeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDeviceForAdmin(id, request.deviceKey()));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableDevice(@PathVariable String id) {
        deviceService.disableDevice(id);
        return ResponseEntity.noContent().build();
    }
}
