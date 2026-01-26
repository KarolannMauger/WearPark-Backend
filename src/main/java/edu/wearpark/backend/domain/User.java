package edu.wearpark.backend.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "users")
public class User {
    @MongoId
    private ObjectId id;
    @Indexed(unique = true)
    private String email;
    private UserAuth auth;
}
