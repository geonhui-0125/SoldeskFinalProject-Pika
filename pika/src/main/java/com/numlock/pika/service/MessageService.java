package com.numlock.pika.service;

import com.numlock.pika.domain.Message;
import com.numlock.pika.domain.Products;
import com.numlock.pika.repository.MessageRepository;
import com.numlock.pika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProductRepository productRepository; // ProductRepository 주입

    @Transactional
    public Message saveMessage(String senderId, String recipientId, String content, Integer productId, boolean isRead) {
        Message.MessageBuilder messageBuilder = Message.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(isRead);

        if (productId != null) {
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));
            messageBuilder.product(product);
        }

        return messageRepository.save(messageBuilder.build());
    }
    @Transactional
    public List<Message> getConversation(String user1Id, String user2Id, Integer productId) {
        if (productId != null) {
        	messageRepository.markAsReadByRoomIdAndUserId(user1Id, user2Id, productId);
            return messageRepository.findConversationByProduct(user1Id, user2Id, productId);
        }
        return messageRepository.findConversationBetweenUsers(user1Id, user2Id);
    }
    
}