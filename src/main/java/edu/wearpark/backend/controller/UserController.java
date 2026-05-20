package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.DeviceUserResponse;
import edu.wearpark.backend.dto.PatchUserRequest;
import edu.wearpark.backend.dto.UserDetailsResponse;
import edu.wearpark.backend.dto.UserSummaryResponse;
import edu.wearpark.backend.repository.DeviceRepository;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.service.DeviceService;
import edu.wearpark.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DeviceService deviceService;
    private final UserRepository userRepo;
    private final DeviceRepository deviceRepo;

    @PostMapping
    ResponseEntity<?> postUser(
            @AuthenticationPrincipal User user,
            @RequestBody PatchUserRequest body
    ) {
        var patchedUser = User.builder()
                .id(user.getId())
                .gender(body.gender())
                .dateOfBirth(body.dateOfBirth())
                .diagnosis(body.diagnosis())
                .firstName(body.firstName())
                .lastName(body.lastName())
                .preferences(body.userPreferences())
                .hasDiagnosis(body.hasDiagnosis())
                .build();
        userService.patchUser(patchedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    ResponseEntity<UserDetailsResponse> getSelf(SecurityContext securityContext) {
        var user = ((DetailedAuthToken) securityContext.getAuthentication()).getPrincipal();

        DeviceUserResponse activeDevice = deviceService.getActiveDeviceForUser(user.getId())
                .map(d -> new DeviceUserResponse(d.getId().toHexString(), d.getDeviceKey(), d.getIsActive()))
                .orElse(null);

        return ResponseEntity.ok(UserDetailsResponse.builder()
                .gender(user.getGender())
                .email(user.getEmail())
                .userPreferences(user.getPreferences())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .dateOfBirth(user.getDateOfBirth())
                .hasDiagnosis(user.getHasDiagnosis())
                .diagnosis(user.getDiagnosis())
                .device(activeDevice)
                .build());
    }
}
