package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MongoTemplate mongoTemplate;
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
}
