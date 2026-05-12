package edu.wearpark.backend.repository;

import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface MotionMonthlyAnalysisRepository extends MongoRepository<MotionMonthlyAnalysis, ObjectId> {
    @Query(
            value= """
                    {
                        'user_id': $0,
                        'start': $1
                    }
                    """
    )
    Optional<MotionMonthlyAnalysis> findByUserIdAndStart(ObjectId userId, Instant start);
}
