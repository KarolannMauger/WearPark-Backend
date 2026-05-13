package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.dto.DeviceAdminResponse;
import edu.wearpark.backend.dto.DeviceUserResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.DeviceRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock DeviceRepository deviceRepo;

    @InjectMocks
    DeviceService deviceService;

    @Nested
    class GetActiveDeviceForUser {

        @Test
        void shouldReturnActiveDevice() {
            ObjectId userId = new ObjectId();
            Device device = Device.builder().id(new ObjectId()).userId(userId).isActive(true).build();
            when(deviceRepo.findByUserIdAndIsActiveTrue(userId)).thenReturn(Optional.of(device));

            Optional<Device> result = deviceService.getActiveDeviceForUser(userId);

            assertTrue(result.isPresent());
            assertEquals(device, result.get());
        }

        @Test
        void shouldReturnEmptyWhenNoActiveDevice() {
            ObjectId userId = new ObjectId();
            when(deviceRepo.findByUserIdAndIsActiveTrue(userId)).thenReturn(Optional.empty());

            Optional<Device> result = deviceService.getActiveDeviceForUser(userId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetDevicesForUser {

        @Test
        void shouldReturnMappedList() {
            ObjectId userId = new ObjectId();
            ObjectId deviceId = new ObjectId();
            Device device = Device.builder()
                    .id(deviceId).userId(userId).deviceKey("key-1").isActive(true)
                    .build();
            when(deviceRepo.findByUserId(userId)).thenReturn(List.of(device));

            List<DeviceUserResponse> result = deviceService.getDevicesForUser(userId);

            assertEquals(1, result.size());
            assertEquals(deviceId.toHexString(), result.get(0).id());
            assertEquals("key-1", result.get(0).deviceKey());
            assertTrue(result.get(0).isActive());
        }

        @Test
        void shouldReturnEmptyListWhenNoDevices() {
            ObjectId userId = new ObjectId();
            when(deviceRepo.findByUserId(userId)).thenReturn(List.of());

            List<DeviceUserResponse> result = deviceService.getDevicesForUser(userId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CreateDeviceForUser {

        @Test
        void shouldCreateAndReturnDevice() {
            ObjectId userId = new ObjectId();
            String key = "key-abc";
            Device saved = Device.builder()
                    .id(new ObjectId()).userId(userId).deviceKey(key).isActive(true)
                    .build();

            when(deviceRepo.findByDeviceKeyAndIsActiveTrue(key)).thenReturn(Optional.empty());
            when(deviceRepo.save(any())).thenReturn(saved);

            DeviceUserResponse result = deviceService.createDeviceForUser(key, userId);

            assertEquals(key, result.deviceKey());
            assertTrue(result.isActive());
            verify(deviceRepo).save(any());
        }

        @Test
        void shouldThrowWhenDeviceKeyAlreadyActive() {
            ObjectId userId = new ObjectId();
            String key = "key-conflict";
            when(deviceRepo.findByDeviceKeyAndIsActiveTrue(key))
                    .thenReturn(Optional.of(Device.builder().build()));

            assertThrows(IllegalStateException.class,
                    () -> deviceService.createDeviceForUser(key, userId));
            verify(deviceRepo, never()).save(any());
        }
    }

    @Nested
    class UpdateDeviceForUser {

        @Test
        void shouldUpdateKeyAndReturn() {
            ObjectId id = new ObjectId();
            Device existing = Device.builder()
                    .id(id).deviceKey("old-key").isActive(true)
                    .build();
            Device saved = Device.builder()
                    .id(id).deviceKey("new-key").isActive(true)
                    .build();

            when(deviceRepo.findById(id)).thenReturn(Optional.of(existing));
            when(deviceRepo.findByDeviceKeyAndIsActiveTrue("new-key")).thenReturn(Optional.empty());
            when(deviceRepo.save(existing)).thenReturn(saved);

            DeviceUserResponse result = deviceService.updateDeviceForUser(id.toHexString(), "new-key");

            assertEquals("new-key", result.deviceKey());
        }

        @Test
        void shouldNotCheckConflictWhenKeyUnchanged() {
            ObjectId id = new ObjectId();
            Device existing = Device.builder()
                    .id(id).deviceKey("same-key").isActive(true)
                    .build();
            when(deviceRepo.findById(id)).thenReturn(Optional.of(existing));
            when(deviceRepo.save(existing)).thenReturn(existing);

            deviceService.updateDeviceForUser(id.toHexString(), "same-key");

            verify(deviceRepo, never()).findByDeviceKeyAndIsActiveTrue(any());
        }

        @Test
        void shouldThrowWhenNewKeyAlreadyActiveElsewhere() {
            ObjectId id = new ObjectId();
            Device existing = Device.builder().id(id).deviceKey("old-key").build();

            when(deviceRepo.findById(id)).thenReturn(Optional.of(existing));
            when(deviceRepo.findByDeviceKeyAndIsActiveTrue("busy-key"))
                    .thenReturn(Optional.of(Device.builder().build()));

            assertThrows(IllegalStateException.class,
                    () -> deviceService.updateDeviceForUser(id.toHexString(), "busy-key"));
        }

        @Test
        void shouldThrowNotFoundWhenDeviceDoesNotExist() {
            ObjectId id = new ObjectId();
            when(deviceRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> deviceService.updateDeviceForUser(id.toHexString(), "any-key"));
        }
    }

    @Nested
    class CreateDeviceForAdmin {

        @Test
        void shouldCreateAndReturnAdminResponse() {
            ObjectId userId = new ObjectId();
            String key = "admin-key";
            Device saved = Device.builder()
                    .id(new ObjectId()).userId(userId).deviceKey(key).isActive(true)
                    .build();

            when(deviceRepo.findByDeviceKeyAndIsActiveTrue(key)).thenReturn(Optional.empty());
            when(deviceRepo.save(any())).thenReturn(saved);

            DeviceAdminResponse result = deviceService.createDeviceForAdmin(userId.toHexString(), key);

            assertEquals(key, result.deviceKey());
            assertTrue(result.isActive());
        }

        @Test
        void shouldThrowWhenDeviceKeyAlreadyActive() {
            ObjectId userId = new ObjectId();
            String key = "conflict";
            when(deviceRepo.findByDeviceKeyAndIsActiveTrue(key))
                    .thenReturn(Optional.of(Device.builder().build()));

            assertThrows(IllegalStateException.class,
                    () -> deviceService.createDeviceForAdmin(userId.toHexString(), key));
        }
    }

    @Nested
    class UpdateDeviceForAdmin {

        @Test
        void shouldUpdateAndReturnAdminResponse() {
            ObjectId id = new ObjectId();
            Device existing = Device.builder().id(id).deviceKey("old").isActive(true).build();
            Device saved = Device.builder().id(id).deviceKey("new").isActive(true).build();

            when(deviceRepo.findById(id)).thenReturn(Optional.of(existing));
            when(deviceRepo.findByDeviceKeyAndIsActiveTrue("new")).thenReturn(Optional.empty());
            when(deviceRepo.save(existing)).thenReturn(saved);

            DeviceAdminResponse result = deviceService.updateDeviceForAdmin(id.toHexString(), "new");

            assertEquals("new", result.deviceKey());
        }
    }

    @Nested
    class DisableDevice {

        @Test
        void shouldDisableActiveDevice() {
            ObjectId id = new ObjectId();
            Device device = Device.builder().id(id).isActive(true).build();
            when(deviceRepo.findById(id)).thenReturn(Optional.of(device));

            deviceService.disableDevice(id.toHexString());

            assertFalse(device.getIsActive());
            assertNotNull(device.getRevokedAt());
            verify(deviceRepo).save(device);
        }

        @Test
        void shouldBeIdempotentWhenAlreadyDisabled() {
            ObjectId id = new ObjectId();
            Device device = Device.builder().id(id).isActive(false).build();
            when(deviceRepo.findById(id)).thenReturn(Optional.of(device));

            deviceService.disableDevice(id.toHexString());

            verify(deviceRepo, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenDeviceDoesNotExist() {
            ObjectId id = new ObjectId();
            when(deviceRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> deviceService.disableDevice(id.toHexString()));
        }
    }
}
