package edu.wearpark.backend.config;

import edu.wearpark.backend.ws.WsAuthInterceptor;
import edu.wearpark.backend.ws.WsMotionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WsMotionHandler motionHandler;
    private final WsAuthInterceptor authInterceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(motionHandler, "/ws/motion")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}
