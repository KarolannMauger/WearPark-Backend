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
@Document(collection = "devices")
public class Device {
    @MongoId
    private ObjectId id;
    @Indexed
    @Field(name = "device_key")
    private String deviceKey;
    @Indexed
    @Field(name = "user_id")
    private ObjectId userId;
}
