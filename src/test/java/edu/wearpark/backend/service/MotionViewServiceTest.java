package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionGraph;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MotionViewServiceTest {

    private MotionEntryRepository motionRepo;
    private MotionViewCacheService cacheService;

    private MotionViewService service;

    @BeforeEach
    void setUp() {
        motionRepo = mock(MotionEntryRepository.class);
        cacheService = mock(MotionViewCacheService.class);

        service = new MotionViewService(
                motionRepo,
                cacheService
        );
    }

    @Test
    void makeDailyAnalysis_shouldReturnCachedAnalysisWhenPresent() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        MotionDailyAnalysis cached = MotionDailyAnalysis.builder()
                .meanAmplitude(42.0)
                .build();

        Instant date = Instant.now().minus(2, ChronoUnit.DAYS);

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.of(cached));

        MotionDailyAnalysis result =
                service.makeDailyAnalysis(date, user);

        assertSame(cached, result);

        verify(motionRepo, never()).findBetween(any(), any(), any());
    }

    @Test
    void makeDailyAnalysis_shouldComputeStatisticsFromMotionEntries() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant date = Instant.parse("2026-01-10T12:00:00Z");

        Instant dayStart = Instant.parse("2026-01-10T00:00:00Z");

        // Create one motion sample
        byte[] bytes = new byte[28];
        MotionDataListWrapper wrapper = new MotionDataListWrapper(bytes);

        MotionDataWrapper sample = wrapper.get();

        sample.setOffsetMs(0);

        // geometric mean = sqrt(3² + 4² + 12²) = 13
        sample.setAx(3.0f);
        sample.setAy(4.0f);
        sample.setAz(12.0f);

        MotionEntry entry = MotionEntry.builder()
                .id(new ObjectId())
                .userId(user.getId())
                .start(dayStart)
                .end(dayStart.plusSeconds(60))
                .nbEntries(1)
                .data(wrapper.getBuffer().array().clone())
                .build();

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of(entry));

        MotionDailyAnalysis result =
                service.makeDailyAnalysis(date, user);

        assertNotNull(result);

        assertEquals(13.0, result.getMeanAmplitude(), 0.0001);
        assertEquals(13.0, result.getPeakAmplitude(), 0.0001);
        assertEquals(0.0, result.getVariance(), 0.0001);

        assertTrue(result.getCoverage() > 0.0);

        assertNotNull(result.getGraph());

        ByteBuffer floatBuffer = ByteBuffer
                .wrap(result.getGraph().getData())
                .order(ByteOrder.LITTLE_ENDIAN);

        float firstValue = floatBuffer.getFloat(0);

        assertEquals(13.0f, firstValue, 0.0001f);

        verify(cacheService)
                .putDailyAnalysis(any(MotionDailyAnalysis.class), eq(user.getId()));
    }

    @Test
    void makeDailyAnalysis_shouldCreateEmptyAnalysisWhenNoEntries() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant date = Instant.now().minus(3, ChronoUnit.DAYS);

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of());

        MotionDailyAnalysis result =
                service.makeDailyAnalysis(date, user);

        assertNotNull(result);

        assertEquals(0.0, result.getCoverage());
        assertEquals(0.0, result.getMeanAmplitude());
        assertEquals(0.0, result.getVariance());

        assertNotNull(result.getGraph());

        MotionGraph graph = result.getGraph();

        assertTrue(Float.isNaN(graph.getMin()));
        assertTrue(Float.isNaN(graph.getMax()));

        verify(cacheService)
                .putDailyAnalysis(result, user.getId());
    }

    @Test
    void makeDailyAnalysis_shouldComputeStatisticsFromGraph() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant start = Instant.now().minus(5, ChronoUnit.DAYS);

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of());

        MotionDailyAnalysis result =
                service.makeDailyAnalysis(start, user);

        assertNotNull(result.getGraph());

        ByteBuffer buffer = ByteBuffer
                .wrap(result.getGraph().getData())
                .order(ByteOrder.LITTLE_ENDIAN);

        assertTrue(buffer.capacity() > 0);
    }

    @Test
    void makeMonthlyAnalysis_shouldReturnCachedAnalysisWhenPresent() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        MotionMonthlyAnalysis cached = MotionMonthlyAnalysis.builder()
                .meanAmplitude(99.0)
                .build();

        Instant date = Instant.now().minus(60, ChronoUnit.DAYS);

        when(cacheService.getMonthlyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.of(cached));

        MotionMonthlyAnalysis result =
                service.makeMonthlyAnalysis(date, user);

        assertSame(cached, result);

        verify(cacheService, never())
                .putDailyAnalysis(any(), any());
    }

    @Test
    void makeMonthlyAnalysis_shouldAggregateDailyAnalyses() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant date = Instant.parse("2026-01-15T00:00:00Z");

        when(cacheService.getMonthlyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of());

        MotionMonthlyAnalysis result =
                service.makeMonthlyAnalysis(date, user);

        assertNotNull(result);

        assertNotNull(result.getDays());
        assertFalse(result.getDays().isEmpty());

        assertEquals(
                result.getDays().size(),
                30
        );

        assertEquals(
                0.0,
                result.getCoverage()
        );

        assertEquals(
                0.0,
                result.getMeanAmplitude()
        );
    }

    @Test
    void makeMonthlyAnalysis_shouldComputeDeltaMeanAmplitude() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant date = Instant.parse("2026-04-15T00:00:00Z");

        when(cacheService.getMonthlyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of());

        MotionMonthlyAnalysis result =
                service.makeMonthlyAnalysis(date, user);

        assertNotNull(result.getDays());

        var firstDay = result.getDays().get(0);
        assertEquals(0.0, firstDay.getDeltaMeanAmplitude());

        if (result.getDays().size() > 1) {
            var secondDay = result.getDays().get(1);

            assertEquals(
                    secondDay.getMeanAmplitude() - firstDay.getMeanAmplitude(),
                    secondDay.getDeltaMeanAmplitude()
            );
        }
    }

    @Test
    void makeDailyAnalysis_shouldPersistComputedAnalysisInCache() {
        User user = User.builder()
                .id(new ObjectId())
                .build();

        Instant date = Instant.now().minus(Duration.ofDays(10));

        when(cacheService.getDailyAnalysis(any(), eq(user.getId())))
                .thenReturn(Optional.empty());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(List.of());

        MotionDailyAnalysis result =
                service.makeDailyAnalysis(date, user);

        verify(cacheService)
                .putDailyAnalysis(result, user.getId());
    }
}