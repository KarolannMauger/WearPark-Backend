package edu.wearpark.backend.config;

import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.security.JwtHttpFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration class responsible for defining Spring Security setting for the application.
 *
 * @version 1.0
 */
@Configuration
@EnableMethodSecurity
public class HttpSecurityConfig {
    /**
     * Defines the password encoder bean used across the authentication process.
     * @return a {@link BCryptPasswordEncoder} INSTANCE for securely hashing password.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the custom authentication manager with
     * - {@link EmailPasswordAuthProvider}.
     * @param emailPasswordAuthProvider the custom provider handling email and password authentication
     * @return a configure {@link AuthenticationManager} instance.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            EmailPasswordAuthProvider emailPasswordAuthProvider
    ) {
        return new ProviderManager(
                emailPasswordAuthProvider
        );
    }

    /**
     * Defines the core security filter chain for all HTTP request.
     * @param httpSecurity the {@link HttpSecurity} configuration builder.
     * @param jwtHttpFilter the {@link JwtHttpFilter} configuration builder.
     * @return a configured {@link SecurityFilterChain} instance that defines the app security rules
     * @throws Exception while building the filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            JwtHttpFilter jwtHttpFilter
    ) throws  Exception{
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth/**", "/public/**", "/ws/**", "/actuator/health").permitAll()
                                .anyRequest().authenticated()
                );
        httpSecurity.addFilterBefore(jwtHttpFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();


        config.setAllowedOrigins(List.of(
                "http://localhost:8081",
                "https://wearpark-app.netlify.app"
        ));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
