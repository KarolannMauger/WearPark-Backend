package edu.wearpark.backend.controller;

import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.AuthService;
import edu.wearpark.backend.service.JwtService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    EmailPasswordAuthProvider emailPasswordAuthProvider;

    @MockitoBean
    JwtService jwtService;

    @Test
    void login_shouldReturnJwt() throws Exception {
        when(authService.login(any())).thenReturn("test.jwt.token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("test.jwt.token"));
    }

    @Test
    void register_shouldReturnNoContent() throws Exception {
        User user = User.builder().id(new ObjectId()).email("test@example.com").build();
        when(authService.register(anyString(), anyString())).thenReturn(user);
        when(authService.generateEmailValidation(any())).thenReturn("validation.jwt");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void validateEmail_shouldReturnNoContent_whenTokenValid() throws Exception {
        when(authService.validateEmail("valid.jwt")).thenReturn(false);

        mockMvc.perform(get("/auth/validate-email/valid.jwt"))
                .andExpect(status().isNoContent());
    }

    @Test
    void validateEmail_shouldReturnNotFound_whenTokenInvalid() throws Exception {
        when(authService.validateEmail("bad.jwt")).thenReturn(true);

        mockMvc.perform(get("/auth/validate-email/bad.jwt"))
                .andExpect(status().isNotFound());
    }
}
