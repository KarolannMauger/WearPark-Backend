package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock
    JwtUtil jwtUtil;
    @Mock
    UserRepository userRepo;

    @InjectMocks
    JwtService jwtService;


    private final String JWT = "test.jwt.token";

    @Test
    void shouldReturnUserWhenClaimsAndUserExist() {
        // arrange
        String userId = new ObjectId().toHexString();
        User user = new User();
        user.setId(new ObjectId(userId));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(userId);

        when(jwtUtil.extractClaims(JWT, "auth"))
                .thenReturn(Optional.of(claims));

        when(userRepo.findById(new ObjectId(userId)))
                .thenReturn(Optional.of(user));

        // act
        Optional<User> result = jwtService.getUserFromAuthToken(JWT);

        // assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());

        verify(jwtUtil).extractClaims(JWT, "auth");
        verify(userRepo).findById(new ObjectId(userId));
    }

}