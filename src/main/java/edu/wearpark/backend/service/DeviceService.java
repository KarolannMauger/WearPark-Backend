package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepo;

    public Device createDeviceForCurrentUser(String deviceKey, ObjectId userId) {
        Optional<Device> existing = deviceRepo
                .findByDeviceKeyAndIsActiveTrue(deviceKey);

        if (existing.isPresent()) {
            throw new IllegalStateException("Device already active on another account");
        }

        Device device = Device.builder()
                .userId(userId)
                .deviceKey(deviceKey)
                .isActive(true)
                .build();

        return deviceRepo.save(device);
    }

    /* TODO : check ownership + ajouter exception de access denied (recommandation) */
    public Device updateDeviceForUser(String deviceId, String newDeviceKey) {
        Device device = deviceRepo.findById(new ObjectId(deviceId))
                .orElseThrow(() -> new NotFoundException("Device not found"));

        if (!device.getDeviceKey().equals(newDeviceKey)) {

            Optional<Device> existing = deviceRepo
                    .findByDeviceKeyAndIsActiveTrue(newDeviceKey);

            if (existing.isPresent()) {
                throw new IllegalStateException("Device already active elsewhere");
            }

            device.setDeviceKey(newDeviceKey);
        }

        return deviceRepo.save(device);
    }

    public Device createDeviceForUserAdmin(String userId, String deviceKey) {
        return createDeviceForCurrentUser(deviceKey, new ObjectId(userId));
    }

    public Device updateDevice(String deviceId, String newDeviceKey) {

        Device device = deviceRepo.findById(new ObjectId(deviceId))
                .orElseThrow(() -> new NotFoundException("Device not found"));

        if (!device.getDeviceKey().equals(newDeviceKey)) {

            Optional<Device> existing = deviceRepo
                    .findByDeviceKeyAndIsActiveTrue(newDeviceKey);

            if (existing.isPresent()) {
                throw new IllegalStateException("Device already active elsewhere");
            }

            device.setDeviceKey(newDeviceKey);
        }

        return deviceRepo.save(device);
    }

    public void disableDevice(String deviceId) {

        Device device = deviceRepo.findById(new ObjectId(deviceId))
                .orElseThrow(() -> new NotFoundException("Device not found"));

        if (Boolean.FALSE.equals(device.getIsActive())) {
            return;
        }

        device.setIsActive(false);
        device.setRevokedAt(Instant.now());

        deviceRepo.save(device);
    }
}