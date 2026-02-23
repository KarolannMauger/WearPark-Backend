package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.MotionViewGraph;
import edu.wearpark.backend.dto.MotionViewGraphExtended;
import edu.wearpark.backend.repository.MotionEntryRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotionViewServiceTest {

    @Mock
    MotionEntryRepository motionRepo;

    @Mock
    Logger log;

    @InjectMocks
    MotionViewService service;


    @Test
    void makeGraph_shouldReturnGraphWithCorrectBounds() {

        Instant start = Instant.parse("2025-01-01T00:00:00Z");
        Instant end   = start.plus(Duration.ofHours(1));
        Duration interval = Duration.ofMinutes(10);

        // no entries
        MotionViewGraph graph = service.makeGraph(
                Collections.emptyList(),
                start,
                end,
                interval
        );

        assertEquals(start, graph.start());
        assertEquals(end, graph.end());
        assertEquals(24, graph.data().length);
        assertEquals(Float.POSITIVE_INFINITY, graph.min());
        assertEquals(Float.NEGATIVE_INFINITY, graph.max());
    }

    @Test
    void makeGraphExtended_shouldReturnEmptyEpisodesWhenNoData() {

        Instant date = Instant.parse("2025-01-01T12:00:00Z");

        User user = new User();
        user.setId(new ObjectId());

        when(motionRepo.findBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        MotionViewGraphExtended result =
                service.makeGraphExtended(date, user, 1.0f);

        assertEquals(0, result.nbEpisode());
        assertEquals(0, result.avgIntensity());
        assertEquals(0, result.avgDurationMs());
        assertNull(result.lastEpisode());
        assertNotNull(result.graph());
    }
}