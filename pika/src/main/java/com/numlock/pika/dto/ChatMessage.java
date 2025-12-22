package com.numlock.pika.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    // 메시지 타입 : 입장, 채팅, 나감
    public enum MessageType {
        ENTER, TALK, QUIT, IMAGE, VIDEO
    }
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String recipientId; // 메시지 받는사람
    private String msg; // 메시지
    private Integer productId; // 상품 ID
}