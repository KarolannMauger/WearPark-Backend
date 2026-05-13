package edu.wearpark.backend.service;

import com.mongodb.client.result.UpdateResult;
import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.UserPreferences;
import edu.wearpark.backend.dto.AdminUserDetailsResponse;
import edu.wearpark.backend.dto.PatchUserAdminRequest;
import edu.wearpark.backend.dto.UserSummaryResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.DeviceRepository;
import edu.wearpark.backend.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock MongoTemplate    mongoTemplate;
    @Mock UserRepository   userRepo;
    @Mock DeviceRepository deviceRepo;

    @InjectMocks
    UserService userService;

    @Nested
    class PatchUser {

        @Test
        void shouldReturnFalseWhenAllFieldsAreNull() {
            User user = new User();
            user.setId(new ObjectId());

            assertFalse(userService.patchUser(user));
        }

        @Test
        void shouldReturnFalseWhenNoDocumentModified() {
            User user = new User();
            user.setId(new ObjectId());
            user.setFirstName("John");

            UpdateResult result = mock(UpdateResult.class);
            when(result.getModifiedCount()).thenReturn(0L);
            when(mongoTemplate.updateFirst(any(), any(), eq(User.class))).thenReturn(result);

            assertFalse(userService.patchUser(user));
        }

        @Test
        void shouldReturnTrueWhenDocumentModified() {
            User user = new User();
            user.setId(new ObjectId());
            user.setGender("MALE");
            user.setLastName("Doe");
            user.setDateOfBirth(Instant.now());
            user.setHasDiagnosis(true);
            user.setDiagnosis("Parkinson");
            user.setPreferences(new UserPreferences());

            UpdateResult result = mock(UpdateResult.class);
            when(result.getModifiedCount()).thenReturn(2L);
            when(mongoTemplate.updateFirst(any(), any(), eq(User.class))).thenReturn(result);

            assertTrue(userService.patchUser(user));
        }
    }

    @Nested
    class SoftDeleteUser {

        @Test
        void shouldMarkUserAsDeleted() {
            ObjectId id = new ObjectId();
            User user = User.builder().id(id).isDeleted(false).build();
            when(userRepo.findById(id)).thenReturn(Optional.of(user));

            userService.softDeleteUser(id.toHexString());

            assertTrue(user.getIsDeleted());
            assertNotNull(user.getDeletedAt());
            verify(userRepo).save(user);
        }

        @Test
        void shouldBeIdempotentWhenAlreadyDeleted() {
            ObjectId id = new ObjectId();
            User user = User.builder().id(id).isDeleted(true).build();
            when(userRepo.findById(id)).thenReturn(Optional.of(user));

            userService.softDeleteUser(id.toHexString());

            verify(userRepo, never()).save(any());
        }

        @Test
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            ObjectId id = new ObjectId();
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.softDeleteUser(id.toHexString()));
        }
    }

    @Nested
    class GetAllUsers {

        @Test
        void shouldReturnMappedPage() {
            ObjectId id = new ObjectId();
            User user = User.builder()
                    .id(id).email("a@b.com")
                    .firstName("Alice").lastName("B")
                    .role("USER").createdAt(Instant.now()).isDeleted(false)
                    .build();

            Pageable pageable = PageRequest.of(0, 20);
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepo.findAll(pageable)).thenReturn(page);

            Page<UserSummaryResponse> result = userService.getAllUsers(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("a@b.com", result.getContent().get(0).email());
            assertEquals(id.toHexString(), result.getContent().get(0).id());
        }

        @Test
        void shouldReturnEmptyPageWhenNoUsers() {
            Pageable pageable = PageRequest.of(0, 20);
            when(userRepo.findAll(pageable)).thenReturn(Page.empty(pageable));

            Page<UserSummaryResponse> result = userService.getAllUsers(pageable);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetAdminUserDetails {

        @Test
        void shouldReturnDetailsWithDevices() {
            ObjectId userId = new ObjectId();
            User user = User.builder()
                    .id(userId).email("a@b.com").role("USER")
                    .createdAt(Instant.now()).isDeleted(false)
                    .build();
            Device device = Device.builder()
                    .id(new ObjectId()).deviceKey("key-123")
                    .userId(userId).isActive(true)
                    .build();

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));
            when(deviceRepo.findByUserId(userId)).thenReturn(List.of(device));

            AdminUserDetailsResponse response =
                    userService.getAdminUserDetails(userId.toHexString());

            assertEquals(userId.toHexString(), response.id());
            assertEquals("a@b.com", response.email());
            assertEquals(1, response.devices().size());
            assertEquals("key-123", response.devices().get(0).deviceKey());
        }

        @Test
        void shouldReturnDetailsWithNoDevices() {
            ObjectId userId = new ObjectId();
            User user = User.builder().id(userId).email("a@b.com").build();

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));
            when(deviceRepo.findByUserId(userId)).thenReturn(List.of());

            AdminUserDetailsResponse response =
                    userService.getAdminUserDetails(userId.toHexString());

            assertTrue(response.devices().isEmpty());
        }

        @Test
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            ObjectId id = new ObjectId();
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.getAdminUserDetails(id.toHexString()));
        }
    }

    @Nested
    class UpdateUserAdmin {

        @Test
        void shouldUpdateAllProvidedFields() {
            ObjectId id = new ObjectId();
            User user = User.builder().id(id).build();
            PatchUserAdminRequest request = new PatchUserAdminRequest(
                    "Alice", "Smith", "ADMIN", "Femme", true, "Parkinson");

            when(userRepo.findById(id)).thenReturn(Optional.of(user));
            when(userRepo.findById(id)).thenReturn(Optional.of(user));
            when(deviceRepo.findByUserId(id)).thenReturn(List.of());

            userService.updateUserAdmin(id.toHexString(), request);

            verify(userRepo).save(user);
            assertEquals("Alice",     user.getFirstName());
            assertEquals("Smith",     user.getLastName());
            assertEquals("ADMIN",     user.getRole());
            assertEquals("Femme",     user.getGender());
            assertEquals("Parkinson", user.getDiagnosis());
        }

        @Test
        void shouldIgnoreNullFields() {
            ObjectId id = new ObjectId();
            User user = User.builder().id(id).firstName("Original").build();
            PatchUserAdminRequest request = new PatchUserAdminRequest(
                    null, null, null, null, null, null);

            when(userRepo.findById(id)).thenReturn(Optional.of(user));
            when(deviceRepo.findByUserId(id)).thenReturn(List.of());

            userService.updateUserAdmin(id.toHexString(), request);

            assertEquals("Original", user.getFirstName());
        }

        @Test
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            ObjectId id = new ObjectId();
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.updateUserAdmin(id.toHexString(),
                            new PatchUserAdminRequest(null, null, null, null, null, null)));
        }
    }

    @Nested
    class UpdateUserRole {

        @Test
        void shouldUpdateRole() {
            ObjectId id = new ObjectId();
            User user = User.builder().id(id).role("USER").build();
            when(userRepo.findById(id)).thenReturn(Optional.of(user));

            userService.updateUserRole(id.toHexString(), "ADMIN");

            assertEquals("ADMIN", user.getRole());
            verify(userRepo).save(user);
        }

        @Test
        void shouldThrowNotFoundWhenUserDoesNotExist() {
            ObjectId id = new ObjectId();
            when(userRepo.findById(id)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> userService.updateUserRole(id.toHexString(), "ADMIN"));
        }
    }
}
