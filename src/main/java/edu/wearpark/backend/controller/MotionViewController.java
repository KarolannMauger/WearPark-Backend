package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.view.MotionViewDailyAnalysis;
import edu.wearpark.backend.dto.view.MotionViewMonthlyAnalysis;
import edu.wearpark.backend.mapper.MotionViewMapper;
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
    ResponseEntity<MotionViewDailyAnalysis> getDay(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "date") Instant date
    ) {
        return ResponseEntity.ok(MotionViewMapper.mapDay(motionViewService.makeDailyAnalysis(date, user)));
    }

    @GetMapping("/month")
    ResponseEntity<MotionViewMonthlyAnalysis> getMonth(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "date") Instant date
    ) {
        return ResponseEntity.ok(MotionViewMapper.mapMonth(motionViewService.makeMonthlyAnalysis(date, user)));
    }

}
