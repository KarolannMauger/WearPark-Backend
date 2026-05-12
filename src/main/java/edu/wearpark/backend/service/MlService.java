package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.Prediction;
import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.dto.MlPredictionResult;
import edu.wearpark.backend.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MlService {

    private final Logger               log;
    private final RestClient           restClient;
    private final PredictionRepository predictionRepo;

    @Value("${ml.hf-space-url}")
    private String hfSpaceUrl;

    @Value("${ml.hf-token}")
    private String hfToken;

    @Async("taskExecutor")
    public void predictAndSave(MotionEntry motionEntry, ObjectId userId) {
        try {
            var base64Data  = Base64.getEncoder().encodeToString(motionEntry.getData());
            var requestBody = Map.of(
                    "base64_data", base64Data,
                    "nb_entries",  motionEntry.getNbEntries()
            );

            var result = restClient.post()
                    .uri(hfSpaceUrl + "/predict/binary")
                    .header("Authorization", "Bearer " + hfToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = "(unreadable)";
                        try { body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8); }
                        catch (IOException ignored) {}
                        log.error("ML service returned HTTP {} for entry {}: {}",
                                res.getStatusCode(), motionEntry.getId(), body);
                        throw new RuntimeException("ML HTTP " + res.getStatusCode() + ": " + body);
                    })
                    .body(MlPredictionResult.class);

            if (result == null) {
                log.error("ML service returned null for motion entry {}", motionEntry.getId());
                return;
            }
            if (result.probability() == null || result.probability().isNaN() || result.probability().isInfinite()) {
                log.warn("ML returned invalid probability ({}) for entry {} — skipping save",
                        result.probability(), motionEntry.getId());
                return;
            }

            var prediction = Prediction.builder()
                    .motionEntryId(motionEntry.getId())
                    .userId(userId)
                    .state(result.state())
                    .label(result.label())
                    .probability(result.probability())
                    .confidence(result.confidence())
                    .prediction(result.prediction())
                    .createdAt(Instant.now())
                    .build();

            predictionRepo.save(prediction);
            log.info("Prediction saved — state={} probability={} entry={}",
                    result.state(), result.probability(), motionEntry.getId());

        } catch (Exception e) {
            log.error("ML prediction failed for motion entry {}", motionEntry.getId(), e);
        }
    }
}
