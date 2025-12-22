package com.numlock.pika.dto;

import com.numlock.pika.domain.Message;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageDto {
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime sentAt;
    private Integer productId;

    public static MessageDto fromEntity(Message message) {
        return MessageDto.builder()
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .productId(message.getProduct() != null ? message.getProduct().getProductId() : null)
                .build();
    }
}
