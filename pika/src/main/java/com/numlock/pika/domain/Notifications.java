package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Table(name = "NOTIFICATIONS")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Notifications {

    @SequenceGenerator(
            name = "notifications_seq_generator",
            sequenceName = "SEQ_NOTIFICATIONS",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "notifications_seq_generator"
    )
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId; // 고유 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 권장
    @JoinColumn(name = "RECEIVER_ID")
    private Users receiver; // 수신자 유저 (FK)

    @Column(name = "TYPE", nullable = false, length = 50)
    private String type; // 알림 구분 코드

    @Column(name = "TITLE", nullable = false, length = 50)
    private String title; // 알림 제목

    @Column(name = "CONTENT", nullable = false, length = 200)
    private String content; // 알림 내용

    @Column(name = "ACTION_URL", length = 100)
    private String actionUrl; // 클릭 시 이동할 경로

    @Builder.Default
    @Column(name = "IS_READ", nullable = false)
    private Integer isRead = 0; // 읽음 상태 (0: 안읽음, 1: 읽음)

    @Builder.Default
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일시
}