package com.numlock.pika.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.user.SimpUserRegistry; // SimpUserRegistry import 추가
import org.springframework.messaging.simp.user.DefaultSimpUserRegistry; // DefaultSimpUserRegistry import 추가

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker // STOMP를 사용하기 위한 어노테이션
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${websocket.allowed-origins}") // 속성 주입
	private String[] allowedOrigins; // 쉼표로 구분된 문자열을 배열로 받음

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/sub"); // 메시지를 구독하는 요청 prefix (destination header)
		config.setApplicationDestinationPrefixes("/pub"); // 메시지를 발행하는 요청 prefix (destination header)
	}

	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // WebSocket/STOMP 연결 엔드포인트
                .setAllowedOrigins(allowedOrigins) // 주입받은 값 사용
                .withSockJS(); // SockJS 지원 (하위 브라우저 호환성)
    }

	@Bean
	public SimpUserRegistry userRegistry() {
		return new DefaultSimpUserRegistry();
	}
}