package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.dto.*;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.JwtService;
import edu.wearpark.backend.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean UserService              userService;
    @MockitoBean UserRepository           userRepo;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService               jwtService;

    private static final String USER_ID = new ObjectId().toHexString();

    private AdminUserDetailsResponse buildDetails() {
        return new AdminUserDetailsResponse(
                USER_ID, "a@b.com", "Alice", "Smith",
                "USER", null, null, false, null,
                Instant.now(), null, false, List.of()
        );
    }

    @Nested
    class GetAllUsers {

        @Test
        void shouldReturnPagedResponse() throws Exception {
            UserSummaryResponse summary = new UserSummaryResponse(
                    USER_ID, "a@b.com", "Alice", "Smith", "USER", Instant.now(), false);

            when(userService.getAllUsers(any()))
                    .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("a@b.com"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturnEmptyPage() throws Exception {
            when(userService.getAllUsers(any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    class GetUser {

        @Test
        void shouldReturnUserDetails() throws Exception {
            when(userService.getAdminUserDetails(USER_ID)).thenReturn(buildDetails());

            mockMvc.perform(get("/admin/users/{id}", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.email").value("a@b.com"));
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.getAdminUserDetails(USER_ID))
                    .thenThrow(new NotFoundException("User not found"));

            mockMvc.perform(get("/admin/users/{id}", USER_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldReturnUpdatedUser() throws Exception {
            PatchUserAdminRequest request = new PatchUserAdminRequest(
                    "Bob", "Jones", "ADMIN", "MALE", false, null);
            AdminUserDetailsResponse updated = new AdminUserDetailsResponse(
                    USER_ID, "a@b.com", "Bob", "Jones",
                    "ADMIN", null, "MALE", false, null,
                    Instant.now(), null, false, List.of()
            );

            when(userService.updateUserAdmin(eq(USER_ID), any())).thenReturn(updated);

            mockMvc.perform(patch("/admin/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Bob"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            PatchUserAdminRequest request = new PatchUserAdminRequest(
                    null, null, null, null, null, null);

            when(userService.updateUserAdmin(eq(USER_ID), any()))
                    .thenThrow(new NotFoundException("User not found"));

            mockMvc.perform(patch("/admin/users/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateUserRole {

        @Test
        void shouldReturn204OnSuccess() throws Exception {
            PatchUserRoleRequest request = new PatchUserRoleRequest("ADMIN");
            doNothing().when(userService).updateUserRole(USER_ID, "ADMIN");

            mockMvc.perform(patch("/admin/users/{id}/role", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            PatchUserRoleRequest request = new PatchUserRoleRequest("ADMIN");
            doThrow(new NotFoundException("User not found"))
                    .when(userService).updateUserRole(USER_ID, "ADMIN");

            mockMvc.perform(patch("/admin/users/{id}/role", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void shouldReturn204OnSuccess() throws Exception {
            doNothing().when(userService).softDeleteUser(USER_ID);

            mockMvc.perform(delete("/admin/users/{id}", USER_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            doThrow(new NotFoundException("User not found"))
                    .when(userService).softDeleteUser(USER_ID);

            mockMvc.perform(delete("/admin/users/{id}", USER_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
