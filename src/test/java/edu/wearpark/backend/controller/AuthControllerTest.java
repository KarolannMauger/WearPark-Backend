package edu.wearpark.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.LoginRequest;
import edu.wearpark.backend.dto.RegisterRequest;
import edu.wearpark.backend.security.token.EmailPasswordAuthToken;
import edu.wearpark.backend.service.AuthService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private AuthService authService;

    @Test
    void login_shouldReturnJwt() throws Exception {
        // arrange
        var request = new LoginRequest("test@email.com", "password");
        when(authService.login(any(EmailPasswordAuthToken.class)))
                .thenReturn("jwt-token");

        // act
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"));

        // assert
        ArgumentCaptor<EmailPasswordAuthToken> captor =
                ArgumentCaptor.forClass(EmailPasswordAuthToken.class);

        verify(authService).login(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("test@email.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password");
    }

    @Test
    void register_shouldReturnNoContent() throws Exception {
        // arrange
        var request = new RegisterRequest("test@email.com", "password");

        var user = mock(User.class);
        when(user.getId()).thenReturn(new ObjectId());

        when(authService.register("test@email.com", "password"))
                .thenReturn(user);
        when(authService.generateEmailValidation(any(ObjectId.class)))
                .thenReturn("email-jwt");

        // act
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // assert
        verify(authService).register("test@email.com", "password");
    }

    @Test
    void validateEmail_shouldReturnNoContent_whenValid() throws Exception {
        // arrange
        when(authService.validateEmail("jwt-token")).thenReturn(false);

        // act
        mockMvc.perform(get("/auth/validate-email/jwt-token"))
                .andExpect(status().isNoContent());

        // assert
        verify(authService).validateEmail("jwt-token");
    }

    @Test
    void validateEmail_shouldReturnError_whenInvalid() throws Exception {
        // arrange
        when(authService.validateEmail("bad-jwt")).thenReturn(true);

        // act
        mockMvc.perform(get("/auth/validate-email/bad-jwt"))
                .andExpect(status().isNotFound());

        // assert
        verify(authService).validateEmail("bad-jwt");
    }
}
