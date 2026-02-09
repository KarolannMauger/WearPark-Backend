package edu.wearpark.backend.repository;

import edu.wearpark.backend.domain.MotionEntry;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MotionEntryRepository extends MongoRepository<MotionEntry, ObjectId> {
    @Query(
            value = """
                {
                    'user_id': ?0,
                    'start': { $gte: ?1, $lte: ?2 }
                }
                """,
            sort = "{ 'start': -1 }"
    )
    List<MotionEntry> findBetween(ObjectId userId, Instant start, Instant end);

    @Query(
            value = "{ 'user_id': ?0 }",
            sort  = "{ 'start': -1 }"
    )
    List<MotionEntry> findLatest(ObjectId userId, Pageable pageable);
}
