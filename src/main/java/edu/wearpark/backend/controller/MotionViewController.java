package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.MotionViewGraphExtended;
import edu.wearpark.backend.service.MotionViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/motion/view")
public class MotionViewController {
    private final MotionViewService motionViewService;
    @GetMapping("/day")
    ResponseEntity<MotionViewGraphExtended> getDay(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "date") Instant date,
            @RequestParam(name = "episodeThreshold", required = false) Float episodeThreshold
    ) {
        if(episodeThreshold == null)
            episodeThreshold = 20.0f;
        return ResponseEntity.ok(motionViewService.makeGraphExtended(date, user, episodeThreshold));
    }
}
