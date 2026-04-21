package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.PagedResponse;
import edu.wearpark.backend.dto.UserSummaryResponse;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepo;

    @GetMapping
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUsers(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        Page<UserSummaryResponse> page = userService.getAllUsers(pageable);

        PagedResponse<UserSummaryResponse> response = new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userRepo.findById(new ObjectId(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }
}