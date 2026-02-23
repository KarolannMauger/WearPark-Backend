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
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotionViewService {
    private final byte[] NAN_FLOAT_LE = new byte[]{ (byte)0x00, (byte) 0x00, (byte) 0xC0, (byte) 0x7F};
    private final MotionEntryRepository motionRepo;
    private final Logger log;
    public MotionViewGraph makeGraph(List<MotionEntry> entries, Instant start, Instant end, Duration interval) {
        final long intervalMs = interval.toMillis();
        final int totalSample = (int) ((end.toEpochMilli() - start.toEpochMilli()) / intervalMs);
        ///
        ByteBuffer buffer = ByteBuffer
                .allocate(totalSample * 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        for(int i = 0; i<buffer.limit(); i+=4)
            buffer.put(i, NAN_FLOAT_LE);

        float sampleMin = Float.POSITIVE_INFINITY;
        float sampleMax = Float.NEGATIVE_INFINITY;

        float   sampleAcc       = 0.0f;
        int     sampleTotal     = 0;
        int     sampleIndex     = 0;
        //
        entryLoop:
        for (MotionEntry entry : entries) {
            var dataList = new MotionDataListWrapper(entry.getData());
            int entryIndexOffset = (int) ((entry.getStart().toEpochMilli()-start.toEpochMilli()) / intervalMs);
            for (int dataIndex = 0; dataIndex < dataList.size(); dataIndex++) {
                MotionDataWrapper data = dataList.get(dataIndex);
                int projectedSampleIndex = entryIndexOffset + (int) (data.offsetMs() / intervalMs);
                //System.out.println(dataIndex);
                if(sampleTotal == 0)
                    sampleIndex = projectedSampleIndex;
                if (sampleIndex < projectedSampleIndex) {
                    // make sure we are not overflowing the buffer
                    if (sampleIndex > totalSample)
                        break entryLoop;
                    // set the data in buffer
                    sampleAcc /= (float) sampleTotal;
                    sampleMin = Math.min(sampleAcc, sampleMin);
                    sampleMax = Math.max(sampleAcc, sampleMax);
                    buffer.putFloat(sampleIndex * 4, sampleAcc);

                    // reset the accumulator
                    sampleAcc   = 0F;
                    sampleTotal = 0;
                    sampleIndex = projectedSampleIndex;
                }
                sampleAcc += data.accGeometricMean();
                sampleTotal += 1;
            }
        } /* motion entries list loop */
        return MotionViewGraph.builder()
                .min(sampleMin)
                .max(sampleMax)
                .end(end)
                .start(start)
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
