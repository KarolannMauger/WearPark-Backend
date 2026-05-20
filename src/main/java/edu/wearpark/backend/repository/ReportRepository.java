package edu.wearpark.backend.repository;

import edu.wearpark.backend.domain.Report;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends MongoRepository<Report, ObjectId> {

    Optional<Report> findByUserIdAndYearAndMonth(ObjectId userId, int year, int month);

    @Query(value = "{ 'user_id': ?0 }", sort = "{ 'generated_at': -1 }")
    List<Report> findHistoryByUserId(ObjectId userId, Pageable pageable);
}
