package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.AdminUserDetailsResponse;
import edu.wearpark.backend.dto.DeviceAdminResponse;
import edu.wearpark.backend.dto.UpdateUserAdminRequest;
import edu.wearpark.backend.dto.UserSummaryResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.DeviceRepository;
import edu.wearpark.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepo;
    private final DeviceRepository deviceRepo;

    public boolean patchUser(User user) {
        Query query = new Query(Criteria.where("id").is(user.getId()));
        Update update = new Update();

        if (user.getGender() != null) {
            update.set("gender", user.getGender());
        }
        if (user.getFirstName() != null) {
            update.set("first_name", user.getFirstName());
        }
        if (user.getLastName() != null) {
            update.set("last_name", user.getLastName());
        }
        if (user.getDateOfBirth() != null) {
            update.set("date_of_birth", user.getDateOfBirth());
        }
        if (user.getHasDiagnosis() != null) {
            update.set("has_diagnosis", user.getHasDiagnosis());
        }
        if (user.getDiagnosis() != null) {
            update.set("diagnosis", user.getDiagnosis());
        }
        if (user.getPreferences() != null) {
            update.set("preferences", user.getPreferences());
        }
        if (update.getUpdateObject().isEmpty()) {
            return false;
        }

        return mongoTemplate
                .updateFirst(query, update, User.class)
                .getModifiedCount() > 0;
    }

    public void softDeleteUser(String id) {
        User user = userRepo.findById(new ObjectId(id))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            return;
        }

        user.setIsDeleted(true);
        user.setDeletedAt(Instant.now());

        userRepo.save(user);
    }

    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(user -> new UserSummaryResponse(
                        user.getId().toHexString(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole(),
                        user.getCreatedAt(),
                        user.getIsDeleted()
                ));
    }

    public AdminUserDetailsResponse getAdminUserDetails(String userId) {

        User user = userRepo.findById(new ObjectId(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<DeviceAdminResponse> devices = deviceRepo.findByUserId(user.getId())
                .stream()
                .map(d -> new DeviceAdminResponse(
                        d.getId().toHexString(),
                        d.getDeviceKey(),
                        d.getIsActive(),
                        d.getCreatedAt(),
                        d.getRevokedAt()
                ))
                .toList();

        return new AdminUserDetailsResponse(
                user.getId().toHexString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getHasDiagnosis(),
                user.getDiagnosis(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getIsDeleted(),
                devices
        );
    }

    public AdminUserDetailsResponse updateUserAdmin(
            String userId,
            UpdateUserAdminRequest request
    ) {

        User user = userRepo.findById(new ObjectId(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.role() != null) user.setRole(request.role());
        if (request.gender() != null) user.setGender(request.gender());

        if (request.diagnosis() != null) user.setDiagnosis(request.diagnosis());

        userRepo.save(user);

        return getAdminUserDetails(userId);
    }

    public void updateUserRole(String userId, String role) {

        User user = userRepo.findById(new ObjectId(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setRole(role);

        userRepo.save(user);
    }
}
