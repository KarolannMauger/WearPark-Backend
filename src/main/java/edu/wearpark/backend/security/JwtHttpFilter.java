package edu.wearpark.backend.security;

import edu.wearpark.backend.repository.UserRepository;
import edu.wearpark.backend.security.token.DetailedAuthToken;
import edu.wearpark.backend.service.JwtService;
import edu.wearpark.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter that intercepts incoming HTTP request
 * and validates the JWT token if present
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtHttpFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final Logger log;
    /**
     * Filters each HTTP request to perform JWT-based authentication.
     *
     * <p>If a valid JWT is found in the authorization header this method retrieves the corresponding user and sets a {@link DetailedAuthToken}.</p>
     *
     * @param request the http request
     * @param response the http response
     * @param filterChain the filter chain
     * @throws ServletException in case of servlet errors
     * @throws IOException case od IO errors
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwt(request);
            jwtService.getUserFromAuthToken(jwt).ifPresent(user -> {
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
                );
                var token = new DetailedAuthToken(user, authorities);
                token.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(token);
            });
        } finally {
            filterChain.doFilter(request,response);
        }
    }

    /**
     * Extracts the JWT token from the Authorization header of the HTTP request.
     *
     * @param request the http request
     * @return the JWT token if present and starts with "Bearer", otherwise {@code null}
     */
    private String extractJwt(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }
}
