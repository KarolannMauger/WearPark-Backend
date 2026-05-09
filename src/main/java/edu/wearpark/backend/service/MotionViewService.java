package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.view.MotionDailyAnalysis;
import edu.wearpark.backend.domain.view.MotionDailySummary;
import edu.wearpark.backend.domain.view.MotionGraph;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.DateUtil;
import edu.wearpark.backend.util.MotionDataListWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotionViewService {
    private final MotionEntryRepository motionRepo;
    private final MotionViewCacheService cacheService;
    private final Duration GRAPH_RESOLUTION = Duration.ofMinutes(1);
    private MotionGraph makeGraph(List<MotionEntry> entries, Instant start, Instant end, Duration resolution) {
        final long binDurationMs    = resolution.toMillis();
        final int binListSize       = (int) ((end.toEpochMilli() - start.toEpochMilli()) / binDurationMs);

        ///
        float[] binAcc  = new float[binListSize];
        Arrays.fill(binAcc, 0.0f);
        int[] binCount  = new int[binListSize];
        Arrays.fill(binCount, 0);

        for (MotionEntry entry : entries) {
            var dataList     = new MotionDataListWrapper(entry.getData());
            long entryDataIndexOffset = (entry.getStart().toEpochMilli() - start.toEpochMilli())/ binDurationMs;
            for(int dataIndex = 0; dataIndex < dataList.size(); dataIndex++) {
                var data = dataList.get(dataIndex);
                int sampleIndex = (int) (entryDataIndexOffset + (data.offsetMs() / binDurationMs));
                if(sampleIndex < 0 || sampleIndex >= binListSize)
                    continue;

                var acc = data.accGeometricMean();
                binAcc[sampleIndex] += acc*acc;
                binCount[sampleIndex] += 1;
            }
        }

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        ByteBuffer buffer = ByteBuffer
                .allocate(binListSize * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i<binListSize; i++) {
            float value;
            if(binCount[i] == 0) {
                value = Float.NaN;
            } else {
                value = (float) Math.sqrt(binAcc[i] / binCount[i]);
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
            buffer.putFloat(i*4, value);
        }
        return MotionGraph.builder()
                .end(end)
                .start(start)
                .min(Float.isFinite(min) ? min : Float.NaN)
                .max(Float.isFinite(max) ? max : Float.NaN)
                .data(buffer.array())
                .build();
    }

    public MotionDailyAnalysis makeDailyAnalysis(Instant date, User user) {
        var interval = DateUtil.getDayInterval(date);
        final Instant start = interval.getFirst();
        final Instant end   = interval.getSecond();

        var cachedAnalysis = cacheService.getDailyAnalysis(start, user.getId());
        if(cachedAnalysis.isPresent())
            return cachedAnalysis.get();

        var entries = motionRepo.findBetween(user.getId(), start, end);
        var graph   = makeGraph(entries, start, end, GRAPH_RESOLUTION);

        FloatBuffer buffer = ByteBuffer
                .wrap(graph.getData())
                .order(ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer();

        double meanAmplitude = 0.0;
        double peakAmplitude = graph.getMax();
        double variance      = 0.0;
        double coverage      = 0.0;
        int    sampleCount   = 0;

        for(var i = 0; i<buffer.limit(); i++) {
            var sample = buffer.get(i);
            if(Float.isNaN(sample))
                continue;
            meanAmplitude += sample;
            peakAmplitude  = Math.max(sample, peakAmplitude);
            sampleCount   += 1;
        }

        if(sampleCount > 0) {
            meanAmplitude /= sampleCount;
            coverage = (double) sampleCount / buffer.limit();
            for(var i = 0; i<buffer.limit(); i++) {
                var sample = buffer.get(i);
                if(Float.isNaN(sample))
                    continue;
                double deviation = sample - meanAmplitude;
                variance += deviation*deviation;
            }
            variance /= sampleCount;
        }

        var analysis =  MotionDailyAnalysis.builder()
                .start(start)
                .end(end)
                .coverage(coverage)
                .meanAmplitude(meanAmplitude)
                .peakAmplitude(peakAmplitude)
                .variance(variance)
                .graph(graph)
                .build();
        cacheService.putDailyAnalysis(analysis, user.getId());
        return analysis;

    }
    public MotionMonthlyAnalysis makeMonthlyAnalysis(Instant date, User user) {
        var interval = DateUtil.getMonthInterval(date);
        final Instant start = interval.getFirst();
        final Instant end   = interval.getSecond();

        var cachedAnalysis = cacheService.getMonthlyAnalysis(start, user.getId());
        if(cachedAnalysis.isPresent())
            return cachedAnalysis.get();

        double coverage         = 0;
        double meanAmplitude    = 0;
        ArrayList<MotionDailySummary> days = new ArrayList<>();
        for(
                var currentDate = start;
                currentDate.isBefore(end);
                currentDate = currentDate.plus(Duration.ofDays(1)))
        {
            var dailyAnalysis = makeDailyAnalysis(currentDate, user);
            var dailySummary = MotionDailySummary
                    .builder()
                    .start(dailyAnalysis.getStart())
                    .end(dailyAnalysis.getEnd())
                    .coverage(dailyAnalysis.getCoverage())
                    .meanAmplitude(dailyAnalysis.getMeanAmplitude())
                    .peakAmplitude(dailyAnalysis.getPeakAmplitude())
                    .variance(dailyAnalysis.getVariance())
                    .deltaMeanAmplitude(0.0)
                    .build();
            try {
                var prevSummary = days.getLast();
                dailySummary.setDeltaMeanAmplitude(dailySummary.getMeanAmplitude() - prevSummary.getMeanAmplitude());
            } catch (Exception ignored){}
            coverage      += dailyAnalysis.getCoverage();
            meanAmplitude += dailyAnalysis.getMeanAmplitude();
            days.add(dailySummary);
        }
        coverage      /= days.size();
        meanAmplitude /= days.size();

        return MotionMonthlyAnalysis.builder()
                //.id()
                //.userId()
                .start(start)
                .end(end)
                .coverage(coverage)
                .meanAmplitude(meanAmplitude)
                .days(days)
                .build();
    }
}
