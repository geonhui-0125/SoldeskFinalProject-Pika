package com.numlock.pika.config;

import com.numlock.pika.handler.NotificationWebSocketHandler;
import com.numlock.pika.handler.WebSockChatHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocket
public class WebSockConfig implements WebSocketConfigurer {

    private final WebSockChatHandler chatWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 채팅 핸들러 등록
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");

        // 알림 핸들러 등록
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .setAllowedOrigins("*");
    }
}