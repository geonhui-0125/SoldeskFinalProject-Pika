package com.numlock.pika.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomDto {
    // 상품 정보
    private Integer productId;
    private String productTitle;
    private String productImage;

    // 상대방 정보
    private String otherUserId;
    private String otherUserNickname;
    private String otherUserProfileImage;

    // 마지막 메시지 정보
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
}
