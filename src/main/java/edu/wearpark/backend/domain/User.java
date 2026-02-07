package edu.wearpark.backend.domain;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@Document(collection = "users")
public class User {
    @MongoId
    private ObjectId id;

    @Indexed(unique = true)
    private String email;

    @Field(name = "is_email_validated")
    private Boolean isEmailValidated;

    private PasswordAuth auth;
}
