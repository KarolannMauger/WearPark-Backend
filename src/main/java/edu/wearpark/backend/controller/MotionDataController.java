package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.domain.MotionData;
import edu.wearpark.backend.domain.MotionEntry;
import edu.wearpark.backend.dto.MotionEntryResponse;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.service.MotionDataService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/motion/data")
public class MotionDataController {
    ObjectMapper mapper = new ObjectMapper();
    @Autowired
    MotionDataService motionDataService;
    @PostMapping
    ResponseEntity<?> postData(
            SecurityContext securityContext,
            @RequestBody String rawBody
    ) {
        try {
            var user = ((DetailedAuthToken)securityContext.getAuthentication()).getPrincipal();
            var body = mapper.readTree(rawBody);
            var data = body.get("data");
            MotionData motionData = MotionData.builder()
                    .ax(motionDataService.parseJsonNode(data.get("ax")))
                    .ay(motionDataService.parseJsonNode(data.get("ay")))
                    .az(motionDataService.parseJsonNode(data.get("az")))
                    .gx(motionDataService.parseJsonNode(data.get("ax")))
                    .gy(motionDataService.parseJsonNode(data.get("ax")))
                    .gz(motionDataService.parseJsonNode(data.get("ax")))
                    .build();
            MotionEntry motionEntry = MotionEntry.builder()
                    .userId(user.getId())
                    .start(Instant.parse(body.get("start").asText()))
                    .end(Instant.parse(body.get("end").asText()))
                    .hz(100)
                    .data(motionData)
                    .build();
            motionDataService.pushData(
                    user,
                    motionEntry
            );
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            throw new AppException(ErrorCode.BAD_REQUEST_BODY);
        }

    }

    @GetMapping
    ResponseEntity<List<MotionEntryResponse>> postData(
            SecurityContext securityContext,
            @RequestParam(required = true) String start,
            @RequestParam(required = false) String end
    ) {
        var user = ((DetailedAuthToken)securityContext.getAuthentication()).getPrincipal();
        var tsStart = Instant.parse(start);
        var tsEnd   = end == null ? Instant.now() : Instant.parse(end);
        var body = motionDataService.getMotionEntries(user, tsStart, tsEnd);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/latest")
    ResponseEntity<MotionEntryResponse> getLatest(
            SecurityContext securityContext
    ) {
        var user = ((DetailedAuthToken)securityContext.getAuthentication()).getPrincipal();
        var body = motionDataService.getLatestMotionEntry(user);
        if(body.isEmpty())
            throw new NotFoundException("motion_entry", "<latest>");
        return ResponseEntity.ok(body.get());
    }
}
