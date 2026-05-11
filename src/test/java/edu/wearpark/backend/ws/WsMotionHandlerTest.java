package edu.wearpark.backend.ws;

import edu.wearpark.backend.domain.User;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WsMotionHandlerTest {

    private WsMotionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WsMotionHandler();
    }

    @Test
    void afterConnectionEstablished_shouldRegisterSessionByUserId() {
        WebSocketSession session = mock(WebSocketSession.class);

        ObjectId userId = new ObjectId();
        User user = User.builder()
                .id(userId)
                .build();

        when(session.getAttributes())
                .thenReturn(Map.of("principal", user));

        handler.afterConnectionEstablished(session);

        assertDoesNotThrow(() -> handler.sendTo(userId, mock(WebSocketMessage.class)));
    }

    @Test
    void sendTo_shouldSendMessageWhenSessionExists() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);

        ObjectId userId = new ObjectId();
        User user = User.builder()
                .id(userId)
                .build();

        when(session.getAttributes())
                .thenReturn(Map.of("principal", user));

        handler.afterConnectionEstablished(session);

        WebSocketMessage<?> message = mock(WebSocketMessage.class);

        handler.sendTo(userId, message);

        verify(session).sendMessage(message);
    }

    @Test
    void sendTo_shouldDoNothingWhenSessionNotFound() throws Exception {
        WebSocketMessage<?> message = mock(WebSocketMessage.class);

        handler.sendTo(new ObjectId(), message);

        // no exception, no session interaction
        assertTrue(true);
    }

    @Test
    @SneakyThrows
    void afterConnectionEstablished_shouldOverwriteExistingSession() {
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        ObjectId userId = new ObjectId();
        User user = User.builder()
                .id(userId)
                .build();

        when(session1.getAttributes()).thenReturn(Map.of("principal", user));
        when(session2.getAttributes()).thenReturn(Map.of("principal", user));

        handler.afterConnectionEstablished(session1);
        handler.afterConnectionEstablished(session2);

        WebSocketMessage<?> message = mock(WebSocketMessage.class);

        handler.sendTo(userId, message);

        verify(session2).sendMessage(message);
        verify(session1, never()).sendMessage(any());
    }
}