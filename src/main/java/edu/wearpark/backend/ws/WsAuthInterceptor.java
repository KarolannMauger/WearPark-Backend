package edu.wearpark.backend.ws;

import edu.wearpark.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContextException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WsAuthInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        MultiValueMap<String, String> query = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams();
        if(!query.containsKey("jwt")){
            return false;
        }

        var jwt = query.get("jwt").get(0);
        var userOptional = jwtService.getUserFromAuthToken(jwt);
        if(userOptional.isEmpty()) {
            return false;
        }
        attributes.put("principal", userOptional.get());
        return true;
    }
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            @Nullable Exception exception
    ) {
    }
}
