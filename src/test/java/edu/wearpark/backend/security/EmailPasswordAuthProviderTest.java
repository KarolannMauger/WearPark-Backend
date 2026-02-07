package edu.wearpark.backend.security;

import edu.wearpark.backend.ErrorCode;
import edu.wearpark.backend.domain.PasswordAuth;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.exception.AppException;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.EmailPasswordAuthToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailPasswordAuthProviderTest {
    @Mock
    UserRepository userRepo;

    PasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
    EmailPasswordAuthProvider EPAuthProvider;

    @BeforeEach
    void setup() {
        EPAuthProvider = new EmailPasswordAuthProvider("5", "t10m");
        EPAuthProvider.userRepo   = userRepo;
        EPAuthProvider.pwdEncoder = pwdEncoder;
    }

    @Nested
    class authenticate {
        @Test
        void whenUnknownAuthToken_throwsError() {
            assertThrows(AuthenticationCredentialsNotFoundException.class, ()->{
                EPAuthProvider.authenticate(null);
            });
        }

        @Test
        void whenNoUser_throwsNotFoundException() {
            // arrange
            when(userRepo.findByEmail(anyString()))
                    .thenReturn(Optional.empty());

            // assert
            assertThrows(NotFoundException.class, ()->{
               EPAuthProvider.authenticate(new EmailPasswordAuthToken("test", "test"));
            });
        }

        @Test
        void whenUserLockout_shouldThrowAppException() {
            // arrange
            when(userRepo.findByEmail(anyString()))
                    .thenReturn(Optional.of(
                            User.builder()
                                    .auth(PasswordAuth.builder().attempts(10).lastAttempt(Instant.now()).build())
                                    .build()
                    ));

            // assert
            var ex = assertThrows(AppException.class, ()->{
                EPAuthProvider.authenticate(new EmailPasswordAuthToken("test", "test"));
            });
            assertEquals(ex.getCode(), ErrorCode.LOCKED_OUT);
        }
        @Test
        void whenWrongPassword_shouldThrowAppException() {
            // arrange
            when(userRepo.findByEmail(anyString()))
                    .thenReturn(Optional.of(
                            User.builder()
                                    .auth(PasswordAuth.builder()
                                            .attempts(0)
                                            .lastAttempt(Instant.now())
                                            .hash(pwdEncoder.encode("teste"))
                                            .build())
                                    .build()
                    ));

            // assert
            var ex = assertThrows(AppException.class, ()->{
                EPAuthProvider.authenticate(new EmailPasswordAuthToken("test", "test"));
            });
            assertEquals(ex.getCode(), ErrorCode.WRONG_PASSWORD);
        }
        @Test
        void happyPath() {
            // arrange
            when(userRepo.findByEmail(anyString()))
                    .thenReturn(Optional.of(
                            User.builder()
                                    .auth(PasswordAuth.builder()
                                            .attempts(0)
                                            .lastAttempt(Instant.now())
                                            .hash(pwdEncoder.encode("test"))
                                            .build())
                                    .build()
                    ));

            // act
            var token = EPAuthProvider.authenticate(new EmailPasswordAuthToken("test", "test"));
        }
    }
}