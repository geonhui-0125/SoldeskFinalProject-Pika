package com.numlock.pika.service;

import com.numlock.pika.domain.Message;
import com.numlock.pika.domain.Products;
import com.numlock.pika.dto.MessageDto; // MessageDto import 추가
import com.numlock.pika.repository.MessageRepository;
import com.numlock.pika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j import 추가
import org.springframework.messaging.simp.SimpMessagingTemplate; // SimpMessagingTemplate import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Slf4j 어노테이션 추가
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProductRepository productRepository; // ProductRepository 주입
    private final SimpMessagingTemplate messagingTemplate; // SimpMessagingTemplate 주입

    @Transactional
    public MessageDto saveMessage(String senderId, String recipientId, String content, Integer productId) {
        Message.MessageBuilder messageBuilder = Message.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false);

        if (productId != null) {
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
            messageBuilder.product(product);
        }

        Message savedMessage = messageRepository.save(messageBuilder.build());
        MessageDto messageDto = MessageDto.fromEntity(savedMessage); // DTO로 변환

        // Send message to sender and recipient via STOMP broker
        // /user/{userId}/queue/messages -> 구독 주소 (수신)
        log.info("Sending message to sender {}: {}", senderId, messageDto);
        messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", messageDto);
        if (!senderId.equals(recipientId)) { // Don't send twice if sender is recipient
            log.info("Sending message to recipient {}: {}", recipientId, messageDto);
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", messageDto);
        }

        return messageDto;
    }

    public List<Message> getConversation(String user1Id, String user2Id, Integer productId) {
        if (productId != null) {
            return messageRepository.findConversationByProduct(user1Id, user2Id, productId);
        }
        return messageRepository.findConversationBetweenUsers(user1Id, user2Id);
    }
}