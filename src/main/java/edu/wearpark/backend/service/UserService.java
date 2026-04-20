package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.UserSummaryResponse;
import edu.wearpark.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepo;

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

    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole(),
                        user.getCreatedAt()
                ));
    }
}
