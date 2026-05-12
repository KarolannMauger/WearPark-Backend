package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.repository.MotionDailyAnalysisRepository;
import edu.wearpark.backend.repository.MotionMonthlyAnalysisRepository;
import edu.wearpark.backend.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MotionViewCacheService {
    private final MotionDailyAnalysisRepository motionDailyAnalysisRepository;
    private final MotionMonthlyAnalysisRepository motionMonthlyAnalysisRepository;

    public void putDailyAnalysis(MotionDailyAnalysis motionDailyAnalysis, ObjectId userId) {
        if(Instant.now().isBefore(motionDailyAnalysis.getEnd()))
            return;
        motionDailyAnalysis.setUserId(userId);
        motionDailyAnalysis.setId(null);
        motionDailyAnalysisRepository.save(motionDailyAnalysis);
    }
    public Optional<MotionDailyAnalysis> getDailyAnalysis(Instant date, ObjectId userId) {
        var requestInterval = DateUtil.getDayInterval(date);
        var currentInterval = DateUtil.getDayInterval(Instant.now());
        if(requestInterval.getFirst().isAfter(currentInterval.getFirst().minusMillis(1)))
            return Optional.empty();
        return motionDailyAnalysisRepository.findByUserIdAndStart(userId, requestInterval.getFirst());
    }
    public void putMonthlyAnalysis(MotionMonthlyAnalysis motionMonthlyAnalysis, ObjectId userId) {
        if(Instant.now().isBefore(motionMonthlyAnalysis.getEnd()))
            return;
        motionMonthlyAnalysis.setUserId(userId);
        motionMonthlyAnalysis.setId(null);
        motionMonthlyAnalysisRepository.save(motionMonthlyAnalysis);
    }
    public Optional<MotionMonthlyAnalysis> getMonthlyAnalysis(Instant date, ObjectId userId) {
        var requestInterval = DateUtil.getMonthInterval(date);
        var currentInterval = DateUtil.getMonthInterval(Instant.now());
        if(requestInterval.getFirst().isAfter(currentInterval.getFirst().minusMillis(1)))
            return Optional.empty();
        return motionMonthlyAnalysisRepository.findByUserIdAndStart(userId, date);
    }
}
