package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.domain.Device;
import edu.wearpark.backend.dto.PatchUserRequest;
import edu.wearpark.backend.repository.DeviceRepository;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.DeviceService;
import edu.wearpark.backend.service.JwtService;
import edu.wearpark.backend.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean UserService              userService;
    @MockitoBean DeviceService            deviceService;
    @MockitoBean UserRepository           userRepo;
    @MockitoBean DeviceRepository         deviceRepo;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService               jwtService;

    @Nested
    class PostUser {

        @Test
        void shouldReturn204WhenPatchSucceeds() throws Exception {
            PatchUserRequest request = new PatchUserRequest(
                    "MALE", "Alice", "Smith", null, false, null, null);

            when(userService.patchUser(any())).thenReturn(true);

            mockMvc.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn204EvenWhenNothingModified() throws Exception {
            PatchUserRequest request = new PatchUserRequest(
                    null, null, null, null, null, null, null);

            when(userService.patchUser(any())).thenReturn(false);

            mockMvc.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    class GetSelf {

        @Test
        void shouldReturnUserDetailsWithActiveDevice() throws Exception {
            ObjectId deviceId = new ObjectId();
            Device device = Device.builder()
                    .id(deviceId).deviceKey("key-1").isActive(true)
                    .build();

            when(deviceService.getActiveDeviceForUser(any())).thenReturn(Optional.of(device));

            mockMvc.perform(get("/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(MockSecurityConfig.defaultUserStub.getEmail()))
                    .andExpect(jsonPath("$.device.deviceKey").value("key-1"))
                    .andExpect(jsonPath("$.device.isActive").value(true));
        }

        @Test
        void shouldReturnUserDetailsWithNullDeviceWhenNoActiveDevice() throws Exception {
            when(deviceService.getActiveDeviceForUser(any())).thenReturn(Optional.empty());

            mockMvc.perform(get("/user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(MockSecurityConfig.defaultUserStub.getEmail()))
                    .andExpect(jsonPath("$.device").doesNotExist());
        }
    }
}
