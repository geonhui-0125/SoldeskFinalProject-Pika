package com.numlock.pika.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numlock.pika.dto.ChatMessage;
import com.numlock.pika.service.MessageService;
import com.numlock.pika.service.Notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Component
public class WebSockChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MessageService messageService; // Inject MessageService
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // WebSocket handshake 과정에서 인증된 사용자의 Principal 정보를 가져옴
        final String userId = session.getPrincipal().getName();
        sessions.put(userId, session);
        log.info("{} connected", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        log.info("Message received: {}", payload);

        // Save message to database

        // Always echo the original message back to the sender's session for display
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        log.info("Message echoed back to sender {}.", chatMessage.getSender());

        WebSocketSession recipientSession = sessions.get(chatMessage.getRecipientId());

        if (recipientSession != null && recipientSession.isOpen()) {
        	messageService.saveMessage(chatMessage.getSender(), chatMessage.getRecipientId(), chatMessage.getMsg(), chatMessage.getProductId(),true);
            // 메시지 다시 직렬화하여 수신자에게 전송
            recipientSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
            log.info("Message sent to recipient {}.", chatMessage.getRecipientId());
        } else {
            // 수신자가 오프라인일 경우 보낸 사람에게 알림 (선택적)
        	messageService.saveMessage(chatMessage.getSender(), chatMessage.getRecipientId(), chatMessage.getMsg(), chatMessage.getProductId(),false);
            log.warn("Recipient {} is not online. Message saved.", chatMessage.getRecipientId());
            String feedbackMsg = "{\"sender\":\"system\", \"msg\":\"User " + chatMessage.getRecipientId() + " is not online. Message saved.\"}";
            session.sendMessage(new TextMessage(feedbackMsg)); // Send system message to sender
            notificationService.sendChatNoti(chatMessage.getSender(), chatMessage.getRecipientId(), chatMessage.getMsg(), chatMessage.getProductId());
        }
        
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String userId = session.getPrincipal().getName();
        sessions.remove(userId);
        log.info("{} disconnected", userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error for session {}: {}", session.getId(), exception.getMessage());
    }
}
