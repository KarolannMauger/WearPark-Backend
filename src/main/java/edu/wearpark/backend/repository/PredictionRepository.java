package edu.wearpark.backend.repository;

import edu.wearpark.backend.domain.Prediction;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends MongoRepository<Prediction, ObjectId> {

    Optional<Prediction> findTopByUserIdOrderByCreatedAtDesc(ObjectId userId);

    @Query(value = "{ 'user_id': ?0 }", sort = "{ 'created_at': -1 }")
    List<Prediction> findHistoryByUserId(ObjectId userId, Pageable pageable);

    @Query("{ 'user_id': ?0, 'created_at': { $gte: ?1, $lt: ?2 } }")
    List<Prediction> findByUserIdAndPeriod(ObjectId userId, Instant start, Instant end);
}
