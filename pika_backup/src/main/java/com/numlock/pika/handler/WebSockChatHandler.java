package com.numlock.pika.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numlock.pika.dto.ChatMessage;
import com.numlock.pika.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class WebSockChatHandler { // No longer extends TextWebSocketHandler

    private final ObjectMapper objectMapper; // Still needed for potential future use or specific parsing
    private final MessageService messageService; // Inject MessageService

    // Handles messages sent to /pub/chat.sendMessage
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        log.info("STOMP Message received from {}: {}", principal.getName(), chatMessage);

        // Ensure sender matches authenticated user (security best practice)
        if (principal == null || !principal.getName().equals(chatMessage.getSender())) {
            log.warn("Unauthorized message sender: {} tried to send as {}", principal != null ? principal.getName() : "unauthenticated", chatMessage.getSender());
            return; // Or throw an exception
        }

        // Save message to database, and MessageService will also handle sending to relevant users.
        messageService.saveMessage(chatMessage.getSender(), chatMessage.getRecipientId(), chatMessage.getMsg(), chatMessage.getProductId());
    }

    // You might add methods for handling /pub/chat.addUser etc.
}
