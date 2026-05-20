package edu.wearpark.backend.controller;

import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.domain.Prediction;
import edu.wearpark.backend.repository.PredictionRepository;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.JwtService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictionController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class PredictionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean PredictionRepository    predictionRepo;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService              jwtService;

    private Prediction buildPrediction() {
        return Prediction.builder()
                .id(new ObjectId("000000000000000000000001"))
                .motionEntryId(new ObjectId("000000000000000000000002"))
                .userId(new ObjectId("000000000000000000000000"))
                .state("parkinson")
                .label("Parkinson")
                .probability(0.82)
                .confidence("high")
                .prediction(1)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
    }

    @Test
    void getLatest_shouldReturnPrediction() throws Exception {
        when(predictionRepo.findTopByUserIdOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.of(buildPrediction()));

        mockMvc.perform(get("/prediction/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("parkinson"))
                .andExpect(jsonPath("$.label").value("Parkinson"))
                .andExpect(jsonPath("$.probability").value(0.82))
                .andExpect(jsonPath("$.confidence").value("high"))
                .andExpect(jsonPath("$.prediction").value(1));
    }

    @Test
    void getLatest_shouldReturn404WhenNoPrediction() throws Exception {
        when(predictionRepo.findTopByUserIdOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/prediction/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistory_shouldReturnList() throws Exception {
        when(predictionRepo.findHistoryByUserId(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(buildPrediction(), buildPrediction()));

        mockMvc.perform(get("/prediction/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].state").value("parkinson"));
    }

    @Test
    void getHistory_shouldReturnEmptyList() throws Exception {
        when(predictionRepo.findHistoryByUserId(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/prediction/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
