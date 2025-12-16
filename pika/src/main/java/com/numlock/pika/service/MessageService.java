package com.numlock.pika.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.numlock.pika.domain.Message;
import com.numlock.pika.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message saveMessage(String senderId, String recipientId, String content) {
        Message message = Message.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false) // Default to unread
                .build();
        return messageRepository.save(message);
    }

    public List<Message> getConversation(String user1Id, String user2Id) {
        return messageRepository.findConversationBetweenUsers(user1Id, user2Id);
    }

    // You might also add methods to mark messages as read, etc.
}