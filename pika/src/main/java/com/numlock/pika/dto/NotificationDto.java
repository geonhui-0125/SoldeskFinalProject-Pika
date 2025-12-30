package com.numlock.pika.dto;

import com.numlock.pika.domain.Notifications;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NotificationDto {

    private Long notificationId;
    private String receiverId;
    private String type;
    private String title;
    private String content;
    private String actionUrl;
    private Integer isRead;
    private LocalDateTime createdAt;
    private String timeAgo;

    public static NotificationDto fromEntity(Notifications notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .receiverId(notification.getReceiver().getId()) // receiverId 추가
                .title(notification.getTitle())
                .content(notification.getContent())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(calculateTimeAgo(notification.getCreatedAt()))
                .build();
    }

    private static String calculateTimeAgo(LocalDateTime createdAt) {
        return ProductDetailDto.calculateTimeAgo(createdAt);
    }
}
