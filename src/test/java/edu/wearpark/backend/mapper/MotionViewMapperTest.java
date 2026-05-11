package edu.wearpark.backend.mapper;

import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionDailySummary;
import edu.wearpark.backend.domain.view.MotionGraph;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.dto.view.MotionViewDailyAnalysis;
import edu.wearpark.backend.dto.view.MotionViewDailySummary;
import edu.wearpark.backend.dto.view.MotionViewGraph;
import edu.wearpark.backend.dto.view.MotionViewMonthlyAnalysis;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MotionViewMapperTest {

    @Test
    void mapGraph_shouldMapAllFieldsAndCloneData() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-01T01:00:00Z");

        var data = new byte[]{0, 1, 2};

        MotionGraph graph = MotionGraph.builder()
                .start(start)
                .end(end)
                .max(10.0f)
                .min(0.5f)
                .data(data)
                .build();

        MotionViewGraph result = MotionViewMapper.mapGraph(graph);

        assertEquals(start, result.start());
        assertEquals(end, result.end());
        assertEquals(10.0F, result.max());
        assertEquals(0.5F, result.min());

        assertArrayEquals(data, result.data());

        // Ensure clone was made
        assertNotSame(data, result.data());

        // Mutating original should not mutate mapped result
        data[0] = 100;
        assertEquals(0, result.data()[0]);
    }

    @Test
    void mapDay_shouldMapAllFieldsIncludingGraph() {
        Instant start = Instant.parse("2026-01-02T00:00:00Z");
        Instant end = Instant.parse("2026-01-02T23:59:59Z");

        MotionGraph graph = MotionGraph.builder()
                .start(start)
                .end(end)
                .max(20.0F)
                .min(1.0F)
                .data(new byte[]{4, 5})
                .build();

        MotionDailyAnalysis analysis = MotionDailyAnalysis.builder()
                .start(start)
                .end(end)
                .coverage(0.95)
                .meanAmplitude(12.5)
                .peakAmplitude(30.0)
                .variance(2.4)
                .graph(graph)
                .build();

        MotionViewDailyAnalysis result = MotionViewMapper.mapDay(analysis);

        assertEquals(start, result.start());
        assertEquals(end, result.end());
        assertEquals(0.95, result.coverage());
        assertEquals(12.5, result.meanAmplitude());
        assertEquals(30.0, result.peakAmplitude());
        assertEquals(2.4, result.variance());

        assertNotNull(result.graph());

        assertEquals(graph.getStart(), result.graph().start());
        assertEquals(graph.getEnd(), result.graph().end());
        assertEquals(graph.getMax(), result.graph().max());
        assertEquals(graph.getMin(), result.graph().min());

        assertArrayEquals(graph.getData(), result.graph().data());
        assertNotSame(graph.getData(), result.graph().data());
    }

    @Test
    void mapMonth_shouldMapAllFieldsAndDays() {
        Instant start = Instant.parse("2026-02-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-28T23:59:59Z");

        MotionDailySummary day1 = MotionDailySummary.builder()
                .start(start)
                .end(start.plusSeconds(86400))
                .coverage(0.80)
                .meanAmplitude(5.0)
                .deltaMeanAmplitude(1.2)
                .build();

        MotionDailySummary day2 = MotionDailySummary.builder()
                .start(start.plusSeconds(86400))
                .end(start.plusSeconds(86400 * 2))
                .coverage(0.90)
                .meanAmplitude(6.5)
                .deltaMeanAmplitude(-0.5)
                .build();

        MotionMonthlyAnalysis monthly = MotionMonthlyAnalysis.builder()
                .start(start)
                .end(end)
                .coverage(0.88)
                .meanAmplitude(5.75)
                .days(List.of(day1, day2))
                .build();

        MotionViewMonthlyAnalysis result = MotionViewMapper.mapMonth(monthly);

        assertEquals(start, result.start());
        assertEquals(end, result.end());
        assertEquals(0.88, result.coverage());
        assertEquals(5.75, result.meanAmplitude());

        assertNotNull(result.days());
        assertEquals(2, result.days().size());

        MotionViewDailySummary mappedDay1 = result.days().getFirst();

        assertEquals(day1.getStart(), mappedDay1.start());
        assertEquals(day1.getEnd(), mappedDay1.end());
        assertEquals(day1.getCoverage(), mappedDay1.coverage());
        assertEquals(day1.getMeanAmplitude(), mappedDay1.meanAmplitude());
        assertEquals(day1.getDeltaMeanAmplitude(), mappedDay1.deltaMeanAmplitude());
    }

    @Test
    void mapToSummary_shouldMapAllFields() {
        Instant start = Instant.parse("2026-03-01T00:00:00Z");
        Instant end = Instant.parse("2026-03-01T23:59:59Z");

        MotionDailySummary summary = MotionDailySummary.builder()
                .start(start)
                .end(end)
                .coverage(0.77)
                .meanAmplitude(8.8)
                .deltaMeanAmplitude(0.9)
                .build();

        MotionViewDailySummary result = MotionViewMapper.mapToSummary(summary);

        assertEquals(start, result.start());
        assertEquals(end, result.end());
        assertEquals(0.77, result.coverage());
        assertEquals(8.8, result.meanAmplitude());
        assertEquals(0.9, result.deltaMeanAmplitude());
    }
}