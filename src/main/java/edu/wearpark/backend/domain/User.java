package edu.wearpark.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    // ids
    @MongoId
    private ObjectId id;
    @Indexed(unique = true)
    private String email;
    @Field(name = "role")
    private String role;

    // user info
    @Field(name = "gender")
    private String gender;
    @Field(name = "first_name")
    private String firstName;
    @Field(name= "last_name")
    private String lastName;
    @Field(name = "date_of_birth")
    private Instant dateOfBirth;
    @Field(name = "has_diagnosis")
    private Boolean hasDiagnosis;
    @Field(name = "diagnosis")
    private String diagnosis;
    @Field(name = "preferences")
    private UserPreferences preferences;

    // user auth
    @Field(name = "is_email_validated")
    private Boolean isEmailValidated;
    @Field(name = "auth")
    private PasswordAuth auth;
}
