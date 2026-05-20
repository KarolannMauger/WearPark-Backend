package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.dto.CreateDeviceRequest;
import edu.wearpark.backend.dto.DeviceUserResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceUserController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class DeviceUserControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean DeviceService             deviceService;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService               jwtService;

    private static final String DEVICE_ID = new ObjectId().toHexString();

    private DeviceUserResponse buildUserResponse(String key) {
        return new DeviceUserResponse(DEVICE_ID, key, true);
    }

    @Nested
    class GetAllDevices {

        @Test
        void shouldReturnDeviceList() throws Exception {
            when(deviceService.getDevicesForUser(any()))
                    .thenReturn(List.of(buildUserResponse("key-1"), buildUserResponse("key-2")));

            mockMvc.perform(get("/devices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].deviceKey").value("key-1"));
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            when(deviceService.getDevicesForUser(any())).thenReturn(List.of());

            mockMvc.perform(get("/devices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class CreateDevice {

        @Test
        void shouldReturnCreatedDevice() throws Exception {
            CreateDeviceRequest request = new CreateDeviceRequest("key-new");
            when(deviceService.createDeviceForUser(eq("key-new"), any()))
                    .thenReturn(buildUserResponse("key-new"));

            mockMvc.perform(post("/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deviceKey").value("key-new"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        void shouldReturn500WhenKeyConflict() throws Exception {
            CreateDeviceRequest request = new CreateDeviceRequest("dup-key");
            when(deviceService.createDeviceForUser(eq("dup-key"), any()))
                    .thenThrow(new IllegalStateException("Device already active on another account"));

            mockMvc.perform(post("/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    class UpdateDevice {

        @Test
        void shouldReturnUpdatedDevice() throws Exception {
            PatchDeviceRequest request = new PatchDeviceRequest("key-updated");
            when(deviceService.updateDeviceForUser(DEVICE_ID, "key-updated"))
                    .thenReturn(buildUserResponse("key-updated"));

            mockMvc.perform(patch("/devices/{id}", DEVICE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deviceKey").value("key-updated"));
        }

        @Test
        void shouldReturn404WhenDeviceNotFound() throws Exception {
            PatchDeviceRequest request = new PatchDeviceRequest("key-updated");
            when(deviceService.updateDeviceForUser(DEVICE_ID, "key-updated"))
                    .thenThrow(new NotFoundException("Device not found"));

            mockMvc.perform(patch("/devices/{id}", DEVICE_ID)
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

            mockMvc.perform(patch("/devices/{id}/disable", DEVICE_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenDeviceNotFound() throws Exception {
            doThrow(new NotFoundException("Device not found"))
                    .when(deviceService).disableDevice(DEVICE_ID);

            mockMvc.perform(patch("/devices/{id}/disable", DEVICE_ID))
                    .andExpect(status().isNotFound());
        }
    }
}
