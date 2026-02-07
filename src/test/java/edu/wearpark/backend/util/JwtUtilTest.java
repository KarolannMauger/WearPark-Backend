package edu.wearpark.backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    SecretKey secret;
    JwtUtil jwtUtil;
    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil("9w038YR7UGBHFJAHISOUDYRGWHREIJOGU89YUHIAWF2130497802375", "t1h");
        jwtUtil.init();
        secret = Keys.hmacShaKeyFor("9w038YR7UGBHFJAHISOUDYRGWHREIJOGU89YUHIAWF2130497802375".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void happyPath_whenGeneratingToken() {
        // act
        var token = jwtUtil.generateToken(
                "subject",
                Instant.now().plus(Duration.ofDays(1)),
                "tbu"
        );

        // assert
        var claims = Jwts
                .parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(claims.getSubject(), "subject");
    }
    @Test
    void happyPath_whenGeneratingTokenWithoutExpiration() {
        var token = jwtUtil.generateToken(
                "subject",
                "tbu"
        );
        var claims = Jwts
                .parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(claims.getSubject(), "subject");
    }
    @Test
    void happyPath_whenExtractingClaims() {
        var token = jwtUtil.generateToken(
                "subject",
                "tbu"
        );
        var claims = jwtUtil.extractClaims(token, "tbu");
        assertTrue(claims.isPresent());
        assertEquals(claims.get().getSubject(), "subject");
    }
    @Test
    void whenMismatchTBU_shouldReturnEmptyOptional() {
        var token = jwtUtil.generateToken("subject", "tbu");

        var claims = jwtUtil.extractClaims(token, "ttt");
        assertTrue(claims.isEmpty());
    }
}