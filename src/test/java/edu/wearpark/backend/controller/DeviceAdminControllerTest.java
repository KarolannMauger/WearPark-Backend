package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.dto.CreateDeviceAdminRequest;
import edu.wearpark.backend.dto.DeviceAdminResponse;
import edu.wearpark.backend.dto.PatchDeviceRequest;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.DeviceService;
import edu.wearpark.backend.service.JwtService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceAdminController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class DeviceAdminControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean DeviceService             deviceService;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService               jwtService;

    private static final String DEVICE_ID = new ObjectId().toHexString();
    private static final String USER_ID   = new ObjectId().toHexString();

    private DeviceAdminResponse buildAdminResponse(String key) {
        return new DeviceAdminResponse(DEVICE_ID, key, true, Instant.now(), null);
    }

    @Nested
    class CreateDevice {

        @Test
        void shouldReturnCreatedDevice() throws Exception {
            CreateDeviceAdminRequest request = new CreateDeviceAdminRequest(USER_ID, "key-123");
            when(deviceService.createDeviceForAdmin(USER_ID, "key-123"))
                    .thenReturn(buildAdminResponse("key-123"));

            mockMvc.perform(post("/admin/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deviceKey").value("key-123"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        void shouldReturn500WhenDeviceKeyConflict() throws Exception {
            CreateDeviceAdminRequest request = new CreateDeviceAdminRequest(USER_ID, "dup-key");
            when(deviceService.createDeviceForAdmin(USER_ID, "dup-key"))
                    .thenThrow(new IllegalStateException("Device already active on another account"));

            mockMvc.perform(post("/admin/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    class UpdateDevice {

        @Test
        void shouldReturnUpdatedDevice() throws Exception {
            PatchDeviceRequest request = new PatchDeviceRequest("new-key");
            when(deviceService.updateDeviceForAdmin(DEVICE_ID, "new-key"))
                    .thenReturn(buildAdminResponse("new-key"));

            mockMvc.perform(patch("/admin/devices/{id}", DEVICE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deviceKey").value("new-key"));
        }

        @Test
        void shouldReturn404WhenDeviceNotFound() throws Exception {
            PatchDeviceRequest request = new PatchDeviceRequest("new-key");
            when(deviceService.updateDeviceForAdmin(DEVICE_ID, "new-key"))
                    .thenThrow(new NotFoundException("Device not found"));

            mockMvc.perform(patch("/admin/devices/{id}", DEVICE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DisableDevice {

        @Test
        void shouldReturn204OnSuccess() throws Exception {
            doNothing().when(deviceService).disableDevice(DEVICE_ID);

            mockMvc.perform(patch("/admin/devices/{id}/disable", DEVICE_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenDeviceNotFound() throws Exception {
            doThrow(new NotFoundException("Device not found"))
                    .when(deviceService).disableDevice(DEVICE_ID);

            mockMvc.perform(patch("/admin/devices/{id}/disable", DEVICE_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
