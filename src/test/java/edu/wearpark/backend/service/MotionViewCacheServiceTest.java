package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.repository.MotionDailyAnalysisRepository;
import edu.wearpark.backend.repository.MotionMonthlyAnalysisRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MotionViewCacheServiceTest {

    private MotionDailyAnalysisRepository dailyRepository;
    private MotionMonthlyAnalysisRepository monthlyRepository;

    private MotionViewCacheService service;

    @BeforeEach
    void setUp() {
        dailyRepository = mock(MotionDailyAnalysisRepository.class);
        monthlyRepository = mock(MotionMonthlyAnalysisRepository.class);

        service = new MotionViewCacheService(
                dailyRepository,
                monthlyRepository
        );
    }

    @Test
    void putDailyAnalysis_shouldSavePastAnalysis() {
        ObjectId userId = new ObjectId();

        MotionDailyAnalysis analysis = MotionDailyAnalysis.builder()
                .end(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        service.putDailyAnalysis(analysis, userId);

        ArgumentCaptor<MotionDailyAnalysis> captor =
                ArgumentCaptor.forClass(MotionDailyAnalysis.class);

        verify(dailyRepository).save(captor.capture());

        MotionDailyAnalysis saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertNull(saved.getId());
    }

    @Test
    void putDailyAnalysis_shouldNotSaveFutureAnalysis() {
        ObjectId userId = new ObjectId();

        MotionDailyAnalysis analysis = MotionDailyAnalysis.builder()
                .end(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        service.putDailyAnalysis(analysis, userId);

        verify(dailyRepository, never()).save(any());
    }

    @Test
    void getDailyAnalysis_shouldReturnEmptyForCurrentDay() {
        ObjectId userId = new ObjectId();

        Optional<MotionDailyAnalysis> result =
                service.getDailyAnalysis(Instant.now(), userId);

        assertTrue(result.isEmpty());

        verify(dailyRepository, never())
                .findByUserIdAndStart(any(), any());
    }

    @Test
    void getDailyAnalysis_shouldQueryRepositoryForPastDay() {
        ObjectId userId = new ObjectId();

        Instant pastDate = Instant.now().minus(3, ChronoUnit.DAYS);

        MotionDailyAnalysis analysis = MotionDailyAnalysis.builder()
                .build();

        when(dailyRepository.findByUserIdAndStart(eq(userId), any()))
                .thenReturn(Optional.of(analysis));

        Optional<MotionDailyAnalysis> result =
                service.getDailyAnalysis(pastDate, userId);

        assertTrue(result.isPresent());
        assertEquals(analysis, result.get());

        verify(dailyRepository)
                .findByUserIdAndStart(eq(userId), any());
    }

    @Test
    void putMonthlyAnalysis_shouldSavePastAnalysis() {
        ObjectId userId = new ObjectId();

        MotionMonthlyAnalysis analysis = MotionMonthlyAnalysis.builder()
                .end(Instant.now().minus(40, ChronoUnit.DAYS))
                .build();

        service.putMonthlyAnalysis(analysis, userId);

        ArgumentCaptor<MotionMonthlyAnalysis> captor =
                ArgumentCaptor.forClass(MotionMonthlyAnalysis.class);

        verify(monthlyRepository).save(captor.capture());

        MotionMonthlyAnalysis saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertNull(saved.getId());
    }

    @Test
    void putMonthlyAnalysis_shouldNotSaveFutureAnalysis() {
        ObjectId userId = new ObjectId();

        MotionMonthlyAnalysis analysis = MotionMonthlyAnalysis.builder()
                .end(Instant.now().plus(10, ChronoUnit.DAYS))
                .build();

        service.putMonthlyAnalysis(analysis, userId);

        verify(monthlyRepository, never()).save(any());
    }

    @Test
    void getMonthlyAnalysis_shouldReturnEmptyForCurrentMonth() {
        ObjectId userId = new ObjectId();

        Optional<MotionMonthlyAnalysis> result =
                service.getMonthlyAnalysis(Instant.now(), userId);

        assertTrue(result.isEmpty());

        verify(monthlyRepository, never())
                .findByUserIdAndStart(any(), any());
    }

    @Test
    void getMonthlyAnalysis_shouldQueryRepositoryForPastMonth() {
        ObjectId userId = new ObjectId();

        Instant pastDate = Instant.now().minus(90, ChronoUnit.DAYS);

        MotionMonthlyAnalysis analysis = MotionMonthlyAnalysis.builder()
                .build();

        when(monthlyRepository.findByUserIdAndStart(eq(userId), eq(pastDate)))
                .thenReturn(Optional.of(analysis));

        Optional<MotionMonthlyAnalysis> result =
                service.getMonthlyAnalysis(pastDate, userId);

        assertTrue(result.isPresent());
        assertEquals(analysis, result.get());

        verify(monthlyRepository)
                .findByUserIdAndStart(userId, pastDate);
    }
}