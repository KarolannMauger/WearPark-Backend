package edu.wearpark.backend.service;

import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.BadRequestException;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.security.token.EmailPasswordAuthToken;
import edu.wearpark.backend.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import(MockSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    JwtUtil jwtUtil;
    @Mock
    UserRepository userRepo;
    @Mock
    AuthenticationManager authManager;
    @Spy
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @InjectMocks
    AuthService authService;

    @Nested
    class login {
        @Test
        void whenLogin_returnGeneratedJWT() {
            // arrange
            var user = User.builder()
                    .id(new ObjectId())
                    .build();
            when(authManager.authenticate(ArgumentMatchers.any()))
                    .thenReturn(new DetailedAuthToken(user, Collections.emptyList()));
            when(jwtUtil.generateToken(user.getId().toHexString(), "login")).thenReturn("j.w.t");
            // act
            var jwt = authService.login(new EmailPasswordAuthToken("test@test.com", "123"));

            // assert
            assertEquals(jwt, "j.w.t");

        }
    }

    @Nested
    class register {
        @Test
        void whenInvalidEmail_throwsBadRequestException() {
            // act
            assertThrows(BadRequestException.class, ()->{
                authService.register("123.123.123", "Test123$");
            });
        }
        @Test
        void whenInvalidPassword_throwsBadRequestException() {
            assertThrows(BadRequestException.class, ()-> authService.register("test@test.com", "test123$"));
        }
        @Test
        void whenUserExists_throwsAppException() {
            // arrange
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(User.builder().build()));

            // assert
            assertThrows(AppException.class, ()-> authService.register("test@test.com", "Test123$"));
        }
        @Test
        void happyPath() {
            // arrange
            when(userRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());
            when(userRepo.save(ArgumentMatchers.any()))
                    .thenReturn(User.builder().email("success").build());

            // act
            var result = authService.register("test@test.com", "Test123$");

            // assert
            assertEquals(result.getEmail(), "success");

        }
    }

    @Nested
    class validateEmail {
        @Test
        void whenClaimsEmpty_shouldReturnFalse() {
            // arrange
            when(jwtUtil.extractClaims(anyString(), anyString())).thenReturn(Optional.empty());

            // act
            assertFalse(authService.validateEmail("j.w.t"));
        }

        @Test
        void whenNoUser_shouldReturnFalse() {
            // arrange
            when(jwtUtil.extractClaims(anyString(), anyString()))
                    .thenReturn(Optional.of(Jwts.claims().subject(new ObjectId().toHexString()).build()));
            when(userRepo.findById(ArgumentMatchers.any()))
                    .thenReturn(Optional.empty());

            // act
            assertFalse(authService.validateEmail("j.w.t"));
        }

        @Test
        void happyPath() {
            // arrange
            when(jwtUtil.extractClaims(anyString(), anyString()))
                    .thenReturn(Optional.of(Jwts.claims().subject(new ObjectId().toHexString()).build()));
            when(userRepo.findById(ArgumentMatchers.any()))
                    .thenReturn(Optional.of(User.builder().build()));
            when(userRepo.save(ArgumentMatchers.any()))
                    .thenReturn(User.builder().build());

            // act
            assertTrue(authService.validateEmail("j.w.t"));

        }
    }

    @Nested
    class generateEmailValidation {
        @Test
        void happyPath() {
            // arrange
            when(jwtUtil.generateToken(anyString(), ArgumentMatchers.any(), anyString()))
                    .thenReturn("j.w.t");

            // act
            assertEquals(authService.generateEmailValidation(new ObjectId()), "j.w.t");
        }
    }
}