package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.dto.CreateDeviceAdminRequest;
import edu.wearpark.backend.dto.UpdateDeviceRequest;
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
    public ResponseEntity<Device> createDevice(
            @RequestBody CreateDeviceAdminRequest request
    ) {
        Device device = deviceService.createDeviceForUserAdmin(
                request.userId(),
                request.deviceKey()
        );

        return ResponseEntity.ok(device);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Device> updateDevice(
            @PathVariable String id,
            @RequestBody UpdateDeviceRequest request
    ) {
        Device device = deviceService.updateDevice(
                id,
                request.deviceKey()
        );

        return ResponseEntity.ok(device);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableDevice(@PathVariable String id) {
        deviceService.disableDevice(id);
        return ResponseEntity.noContent().build();
    }
}
