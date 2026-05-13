package edu.wearpark.backend.repository;


import edu.wearpark.backend.domain.Device;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends MongoRepository<Device, ObjectId> {
    Optional<Device> findByDeviceKey(String deviceKey);

    Optional<Device> findByDeviceKeyAndIsActiveTrue(String deviceKey);

    Optional<Device> findByUserIdAndIsActiveTrue(ObjectId userId);

    List<Device> findByUserId(ObjectId userId);
}
