package edu.wearpark.backend.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Utility class to handle JWT
 */
@Component
public class JwtUtil {
    final private String JWT_SECRET;
    final private Duration JWT_EXPIRATION;
    private SecretKey key;

    /**
     * Create a new {@link JwtUtil}
     * @param jwtSecret depends on props <code>auth.jwt.secret</code>
     * @param jwtExpiration depends on props <code>auth.jwt.expiration</code>
     */
    JwtUtil(
            @Value("${auth.jwt.secret}") final String jwtSecret,
            @Value("${auth.jwt.expiration}") final String jwtExpiration
    ) {
        JWT_SECRET = jwtSecret;
        JWT_EXPIRATION = DurationCodec.decode(jwtExpiration);
    }
    /** Initialize the secret key */
    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a new JWT given a subject
     * @param subject the subject to pass in the JWT
     * @return the computed JWT
     */
    public String generateToken(String subject, String tbu) {
        return generateToken(subject, Instant.now().plus(JWT_EXPIRATION), tbu);
    }
    public String generateToken(String subject, Instant expiresAt, String tbu) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .claim("tbu", tbu)
                .signWith(key)
                .compact();
    }

    /**
     * Validate a JWT token
     * @param token the token
     */
    public Optional<Claims> extractClaims(String token, String tbu) throws RuntimeException {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload();
            if(!claims.get("tbu").equals(tbu)) return Optional.empty();
            return Optional.of(claims);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}