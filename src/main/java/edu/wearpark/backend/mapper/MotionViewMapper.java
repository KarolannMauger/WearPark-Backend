package edu.wearpark.backend.mapper;

import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionDailySummary;
import edu.wearpark.backend.domain.view.MotionGraph;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.dto.view.MotionViewDailyAnalysis;
import edu.wearpark.backend.dto.view.MotionViewDailySummary;
import edu.wearpark.backend.dto.view.MotionViewGraph;
import edu.wearpark.backend.dto.view.MotionViewMonthlyAnalysis;

public class MotionViewMapper {
    private MotionViewMapper(){}
    static public MotionViewGraph mapGraph(MotionGraph motionGraph) {
        return MotionViewGraph.builder()
                .start(motionGraph.getStart())
                .end(motionGraph.getEnd())
                .max(motionGraph.getMax())
                .min(motionGraph.getMin())
                .data(motionGraph.getData().clone())
                .build();
    }
    static public MotionViewDailyAnalysis mapDay(MotionDailyAnalysis motionDailyAnalysis) {
        return MotionViewDailyAnalysis.builder()
                .start(motionDailyAnalysis.getStart())
                .end(motionDailyAnalysis.getEnd())
                .coverage(motionDailyAnalysis.getCoverage())
                .meanAmplitude(motionDailyAnalysis.getMeanAmplitude())
                .peakAmplitude(motionDailyAnalysis.getPeakAmplitude())
                .variance(motionDailyAnalysis.getVariance())
                .graph(mapGraph(motionDailyAnalysis.getGraph()))
                .build();
    }
    static public MotionViewMonthlyAnalysis mapMonth(MotionMonthlyAnalysis motionMonthlyAnalysis) {
        return MotionViewMonthlyAnalysis.builder()
                .start(motionMonthlyAnalysis.getStart())
                .end(motionMonthlyAnalysis.getEnd())
                .coverage(motionMonthlyAnalysis.getCoverage())
                .meanAmplitude(motionMonthlyAnalysis.getMeanAmplitude())
                .days(motionMonthlyAnalysis
                        .getDays()
                        .stream()
                        .map(MotionViewMapper::mapToSummary)
                        .toList()
                )
                .build();
    }
    static public MotionViewDailySummary mapToSummary(MotionDailySummary motionDailySummary) {
        return MotionViewDailySummary.builder()
                .start(motionDailySummary.getStart())
                .end(motionDailySummary.getEnd())
                .coverage(motionDailySummary.getCoverage())
                .meanAmplitude(motionDailySummary.getMeanAmplitude())
                .deltaMeanAmplitude(motionDailySummary.getDeltaMeanAmplitude())
                .build();
    }
}
