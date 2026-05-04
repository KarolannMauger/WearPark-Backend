package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.dto.DeviceAdminResponse;
import edu.wearpark.backend.dto.DeviceUserResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepo;

    private Device findById(String deviceId) {
        return deviceRepo.findById(new ObjectId(deviceId))
                .orElseThrow(() -> new NotFoundException("Device not found"));
    }

    private Device saveNewDevice(String deviceKey, ObjectId userId) {
        if (deviceRepo.findByDeviceKeyAndIsActiveTrue(deviceKey).isPresent()) {
            throw new IllegalStateException("Device already active on another account");
        }
        return deviceRepo.save(Device.builder()
                .userId(userId)
                .deviceKey(deviceKey)
                .isActive(true)
                .build());
    }

    private Device saveUpdatedKey(String deviceId, String newDeviceKey) {
        Device device = findById(deviceId);
        if (!device.getDeviceKey().equals(newDeviceKey)) {
            if (deviceRepo.findByDeviceKeyAndIsActiveTrue(newDeviceKey).isPresent()) {
                throw new IllegalStateException("Device already active elsewhere");
            }
            device.setDeviceKey(newDeviceKey);
        }
        return deviceRepo.save(device);
    }

    public Optional<Device> getActiveDeviceForUser(ObjectId userId) {
        return deviceRepo.findByUserIdAndIsActiveTrue(userId);
    }

    public List<DeviceUserResponse> getDevicesForUser(ObjectId userId) {
        return deviceRepo.findByUserId(userId).stream()
                .map(this::toUserResponse)
                .toList();
    }

    public DeviceUserResponse createDeviceForUser(String deviceKey, ObjectId userId) {
        return toUserResponse(saveNewDevice(deviceKey, userId));
    }

    public DeviceUserResponse updateDeviceForUser(String deviceId, String newDeviceKey) {
        return toUserResponse(saveUpdatedKey(deviceId, newDeviceKey));
    }

    public DeviceAdminResponse createDeviceForAdmin(String userId, String deviceKey) {
        return toAdminResponse(saveNewDevice(deviceKey, new ObjectId(userId)));
    }

    public DeviceAdminResponse updateDeviceForAdmin(String deviceId, String newDeviceKey) {
        return toAdminResponse(saveUpdatedKey(deviceId, newDeviceKey));
    }

    public void disableDevice(String deviceId) {
        Device device = findById(deviceId);
        if (Boolean.TRUE.equals(device.getIsActive())) {
            device.setIsActive(false);
            device.setRevokedAt(Instant.now());
            deviceRepo.save(device);
        }
    }

    private DeviceUserResponse toUserResponse(Device d) {
        return new DeviceUserResponse(d.getId().toHexString(), d.getDeviceKey(), d.getIsActive());
    }

    private DeviceAdminResponse toAdminResponse(Device d) {
        return new DeviceAdminResponse(
                d.getId().toHexString(),
                d.getDeviceKey(),
                d.getIsActive(),
                d.getCreatedAt(),
                d.getRevokedAt()
        );
    }
}