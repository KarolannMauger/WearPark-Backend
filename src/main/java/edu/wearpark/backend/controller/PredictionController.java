package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.PredictionResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/prediction")
public class PredictionController {

    private final PredictionRepository predictionRepo;

    @GetMapping("/latest")
    ResponseEntity<PredictionResponse> getLatest(
            @AuthenticationPrincipal User user
    ) {
        var prediction = predictionRepo.findTopByUserIdOrderByCreatedAtDesc(user.getId());
        if (prediction.isEmpty())
            throw new NotFoundException("prediction");
        return ResponseEntity.ok(PredictionResponse.from(prediction.get()));
    }

    @GetMapping("/history")
    ResponseEntity<List<PredictionResponse>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var predictions = predictionRepo.findHistoryByUserId(
                user.getId(),
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(predictions.stream().map(PredictionResponse::from).toList());
    }
}
