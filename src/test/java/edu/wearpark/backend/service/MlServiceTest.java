package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.domain.Prediction;
import edu.wearpark.backend.dto.MlPredictionResult;
import edu.wearpark.backend.repository.PredictionRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MlServiceTest {

    @Mock private Logger               log;
    @Mock private PredictionRepository predictionRepo;

    // RETURNS_SELF handles the fluent builder chain automatically
    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient restClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.ResponseSpec responseSpec;

    private MlService mlService;

    @BeforeEach
    void setUp() {
        mlService = new MlService(log, restClient, predictionRepo);
        ReflectionTestUtils.setField(mlService, "hfSpaceUrl", "https://test.hf.space");
        ReflectionTestUtils.setField(mlService, "hfToken",    "hf_test_token");

        // Wire the chain: restClient.post() → requestBodyUriSpec → (self) → retrieve() → responseSpec
        // lenient: some tests override restClient.post() so retrieve() stub may go unused
        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldSavePredictionWhenMlReturnsResult() {
        var motionEntry = MotionEntry.builder()
                .id(new ObjectId())
                .userId(new ObjectId())
                .nbEntries(1000)
                .data(new byte[28 * 1000])
                .build();
        var userId   = new ObjectId();
        var mlResult = new MlPredictionResult(1, 0.82d, "parkinson", "Parkinson", "high");

        when(responseSpec.body(MlPredictionResult.class)).thenReturn(mlResult);

        mlService.predictAndSave(motionEntry, userId);

        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(predictionRepo).save(captor.capture());

        var saved = captor.getValue();
        assertEquals("parkinson",        saved.getState());
        assertEquals("Parkinson",        saved.getLabel());
        assertEquals(0.82,               saved.getProbability());
        assertEquals("high",             saved.getConfidence());
        assertEquals(1,                  saved.getPrediction());
        assertEquals(userId,             saved.getUserId());
        assertEquals(motionEntry.getId(), saved.getMotionEntryId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldNotSaveWhenMlReturnsNull() {
        var motionEntry = MotionEntry.builder()
                .id(new ObjectId())
                .nbEntries(1000)
                .data(new byte[28 * 1000])
                .build();

        when(responseSpec.body(MlPredictionResult.class)).thenReturn(null);

        mlService.predictAndSave(motionEntry, new ObjectId());

        verify(predictionRepo, never()).save(any());
    }

    @Test
    void shouldNotThrowWhenRestClientFails() {
        var motionEntry = MotionEntry.builder()
                .id(new ObjectId())
                .nbEntries(1000)
                .data(new byte[28 * 1000])
                .build();

        when(restClient.post()).thenThrow(new RuntimeException("connection refused"));

        assertDoesNotThrow(() -> mlService.predictAndSave(motionEntry, new ObjectId()));
        verify(predictionRepo, never()).save(any());
    }
}
