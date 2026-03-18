package edu.wearpark.backend.ws;

import edu.wearpark.backend.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WsMotionHandler extends TextWebSocketHandler {
    private Map<ObjectId, WebSocketSession> sessions =
            new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        User user = (User) session.getAttributes().get("principal");
        sessions.put(user.getId(), session);
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {
        sessions.values().remove(session);
    }

    public void sendTo(ObjectId userId, WebSocketMessage<?> message) throws IOException {
        var session = sessions.get(userId);
        if(session != null)
            session.sendMessage(message);
    }
}
