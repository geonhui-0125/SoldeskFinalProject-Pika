package com.numlock.pika.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numlock.pika.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * WebSocket 연결이 성공적으로 수립되었을 때 호출됩니다.
     * 사용자의 Principal 이름(사용자 ID)을 키로 하여 세션을 저장합니다.
     * @param session WebSocket 세션
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (session.getPrincipal() == null) {
            log.warn("WebSocket session {} connected without a principal. Closing session.", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION.withReason("User not authenticated"));
            return;
        }
        final String userId = session.getPrincipal().getName();
        sessions.put(userId, session);
        log.info("[Notification] User {} connected. Session ID: {}", userId, session.getId());
    }

    /**
     * WebSocket 연결이 닫혔을 때 호출됩니다.
     * 저장된 세션 맵에서 해당 사용자의 세션을 제거합니다.
     * @param session WebSocket 세션
     * @param status 연결 종료 상태
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (session.getPrincipal() != null) {
            final String userId = session.getPrincipal().getName();
            sessions.remove(userId);
            log.info("[Notification] User {} disconnected. Session ID: {}. Status: {}", userId, session.getId(), status);
        } else {
            log.warn("[Notification] A session without a principal was closed. Session ID: {}", session.getId());
        }
    }

    /**
     * 특정 사용자에게 알림 메시지를 전송합니다.
     * 이 메서드는 NotificationService에서 호출되어 실시간 알림을 클라이언트에게 푸시합니다.
     * @param userId 메시지를 받을 사용자의 ID
     * @param notificationDto 전송할 알림 데이터
     */
    public void sendNotification(String userId, NotificationDto notificationDto) {
        WebSocketSession session = sessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                String messagePayload = objectMapper.writeValueAsString(notificationDto);
                session.sendMessage(new TextMessage(messagePayload));
                log.info("[Notification] Sent notification to user {}: {}", userId, messagePayload);
            } catch (IOException e) {
                log.error("[Notification] Failed to send message to user {}. Error: {}", userId, e.getMessage());
            }
        } else {
            log.warn("[Notification] User {} is not connected. Notification will not be sent in real-time.", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 오는 메시지를 처리할 로직 (필요시 구현)
        // 예: 클라이언트가 'ping'을 보내 연결을 유지하거나, 알림을 읽음 처리했음을 알릴 수 있습니다.
        log.info("[Notification] Received message from {}: {}", session.getPrincipal().getName(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[Notification] Transport error for session {}: {}", session.getId(), exception.getMessage());
    }
}
