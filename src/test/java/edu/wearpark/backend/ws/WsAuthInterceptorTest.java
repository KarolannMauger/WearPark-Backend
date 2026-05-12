package edu.wearpark.backend.ws;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WsAuthInterceptorTest {

    private JwtService jwtService;
    private WsAuthInterceptor interceptor;

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private WebSocketHandler handler;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        interceptor = new WsAuthInterceptor(jwtService);

        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        handler = mock(WebSocketHandler.class);

        attributes = new HashMap<>();
    }

    @Test
    void beforeHandshake_shouldRejectWhenNoJwtParam() throws Exception {
        when(request.getURI())
                .thenReturn(new URI("ws://localhost/ws"));

        boolean result = interceptor.beforeHandshake(
                request, response, handler, attributes
        );

        assertFalse(result);
        assertTrue(attributes.isEmpty());

        verifyNoInteractions(jwtService);
    }

    @Test
    void beforeHandshake_shouldRejectWhenJwtInvalid() throws Exception {
        when(request.getURI())
                .thenReturn(new URI("ws://localhost/ws?jwt=token123"));

        when(jwtService.getUserFromAuthToken("token123"))
                .thenReturn(Optional.empty());

        boolean result = interceptor.beforeHandshake(
                request, response, handler, attributes
        );

        assertFalse(result);
        assertTrue(attributes.isEmpty());

        verify(jwtService).getUserFromAuthToken("token123");
    }

    @Test
    void beforeHandshake_shouldAcceptAndSetPrincipal() throws Exception {
        when(request.getURI())
                .thenReturn(new URI("ws://localhost/ws?jwt=valid-token"));

        User user = User.builder().build();

        when(jwtService.getUserFromAuthToken("valid-token"))
                .thenReturn(Optional.of(user));

        boolean result = interceptor.beforeHandshake(
                request, response, handler, attributes
        );

        assertTrue(result);
        assertEquals(user, attributes.get("principal"));

        verify(jwtService).getUserFromAuthToken("valid-token");
    }

    @Test
    void afterHandshake_shouldDoNothing() {
        assertDoesNotThrow(() ->
                interceptor.afterHandshake(request, response, handler, null)
        );
    }
}