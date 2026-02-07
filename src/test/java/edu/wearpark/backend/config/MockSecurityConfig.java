package edu.wearpark.backend.config;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import org.bson.types.ObjectId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;
import java.util.Optional;

/**
 * This is a configuration class that allow to populate the SecurityContext with an Authentication
 */
@TestConfiguration
public class MockSecurityConfig {
    final public static User defaultUserStub = User.builder()
            .id(new ObjectId("0"))
            .email("test@user.com")
            .build();
    public static class AuthMiddleware {
        public Optional<DetailedAuthToken> getAuthToken() {
            var token = new DetailedAuthToken(defaultUserStub, Collections.emptyList());
            token.setAuthenticated(true);
            return Optional.of(token);
        }
    }
    @Bean
    public AuthenticationManager authenticationManager(
            EmailPasswordAuthProvider emailPasswordAuthProvider
    ) {
        return new ProviderManager(
                emailPasswordAuthProvider
        );
    }
    @Bean
    public AuthMiddleware defaultAuthFilter() {
        return new AuthMiddleware();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthMiddleware authFilter) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                );
        http.addFilterBefore((req, res, fc) -> {
            authFilter.getAuthToken().ifPresent(authToken->{
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
            fc.doFilter(req, res);
        }, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}