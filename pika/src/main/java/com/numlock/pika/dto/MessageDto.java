package com.numlock.pika.dto;

import com.numlock.pika.domain.Message;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class MessageDto {
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime sentAt;
    private Integer productId;
    private String sentAtFormatted; // 포맷된 시간 문자열 추가

    public static MessageDto fromEntity(Message message) {
        String formattedTime = null;
        if (message.getSentAt() != null) {
            // 메시지 날짜가 오늘인지 확인
            if (message.getSentAt().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                // 오늘 보낸 메시지는 시간만 표시 (예: 14:30)
                formattedTime = message.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                // 어제 또는 그 이전에 보낸 메시지는 날짜만 표시 (예: 2024-07-29)
                formattedTime = message.getSentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }
            return MessageDto.builder()
                    .senderId(message.getSenderId())
                    .recipientId(message.getRecipientId())
                    .content(message.getContent())
                    .sentAt(message.getSentAt())
                    .productId(message.getProduct() != null ? message.getProduct().getProductId() : null)
                    .sentAtFormatted(formattedTime) // 포맷된 시간 설정
                    .build();
        }
}
