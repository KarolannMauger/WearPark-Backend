package edu.wearpark.backend.repository;

import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface MotionDailyAnalysisRepository extends MongoRepository<MotionDailyAnalysis, ObjectId> {
    @Query(
            value= """
                    {
                        'user_id': $0,
                        'start': $1
                    }
                    """
    )
    Optional<MotionDailyAnalysis> findByUserIdAndStart(ObjectId userId, Instant start);
}
