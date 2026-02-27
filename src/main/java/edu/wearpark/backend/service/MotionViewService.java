package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.MotionViewGraphExtended;
import edu.wearpark.backend.dto.MotionViewGraph;
import edu.wearpark.backend.repository.MotionEntryRepository;
import edu.wearpark.backend.util.MotionDataListWrapper;
import edu.wearpark.backend.util.MotionDataWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotionViewService {
    private final MotionEntryRepository motionRepo;

    public MotionViewGraph makeGraph(List<MotionEntry> entries, Instant start, Instant end, Duration interval) {
        final long binDurationMs   = interval.toMillis();
        final int binListSize     = (int) ((end.toEpochMilli() - start.toEpochMilli()) / binDurationMs);

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
        float max = Float.POSITIVE_INFINITY;
        ByteBuffer buffer = ByteBuffer
                .allocate(binListSize * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i<binListSize; i++) {
            float mean =  binAcc[i] / binCount[i];
            float value = (float) Math.sqrt(mean);
            if(!Float.isNaN(value)) {
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
            buffer.putFloat(i*4, value);
        }
        return MotionViewGraph.builder()
                .end(end)
                .start(start)
                .min(Float.isFinite(min) ? min : Float.NaN)
                .max(Float.isFinite(max) ? max : Float.NaN)
                .data(buffer.array())
                .build();
    }
    public MotionViewGraphExtended makeGraphExtended(Instant date, User user, float episodeThreshold) {
        // floor the date to the current day
        Instant start = date.atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        Instant end = start.plus(Duration.ofDays(1));

        // get the motions data
        var interval = Duration.ofMinutes(10);
        var entries = motionRepo.findBetween(user.getId(), start, end);
        var graph   = makeGraph(
                entries,
                start,
                end,
                interval
        );
        ///
        FloatBuffer buffer = ByteBuffer
                .wrap(graph.data())
                .order(ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer();

        int     totalEpisode    = 0;
        int     totalDurationMs = 0;
        int     lastEpisode     = -1;
        float   totalIntensity  = 0.0f;

        int   episodeTotalSample = 0;
        int   episodeStart = 0;
        float episodeSampleAcc = 0.0f;

        for(int i = 0; i<buffer.limit(); i++) {
            float sample = buffer.get(i);
            if(Float.isNaN(sample) || sample < episodeThreshold) {
                if(episodeTotalSample == 0)
                    continue;
                totalEpisode    += 1;
                totalDurationMs += interval.toMillis()*(i-episodeStart);
                totalIntensity  += episodeSampleAcc / episodeTotalSample;
                lastEpisode      = episodeStart;
                episodeTotalSample  = 0;
                episodeStart        = 0;
                episodeSampleAcc    = 0.0f;
            } else {
                if (episodeTotalSample == 0)
                    episodeStart = i;
                episodeSampleAcc += sample;
                episodeTotalSample += 1;
            }
        }

        ///
        return MotionViewGraphExtended
                .builder()
                .date(start)
                .avgIntensity(totalEpisode > 0 ? totalIntensity/totalEpisode : 0)
                .avgDurationMs(totalEpisode > 0 ?  totalDurationMs/totalEpisode : 0)
                .nbEpisode(totalEpisode)
                .lastEpisode(lastEpisode == -1 ? null : start.plus(Duration.ofMillis(interval.toMillis()*lastEpisode)))
                .graph(graph)
                .build();
    }
}
