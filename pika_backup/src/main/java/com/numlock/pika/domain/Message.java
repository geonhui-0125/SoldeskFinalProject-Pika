package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MESSAGES") // Oracle에서는 TABLE_NAME이 길어지지 않도록 주의
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq")
    @SequenceGenerator(name = "message_seq", sequenceName = "MESSAGE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    private Products product;

    @Column(name = "SENDER_ID", nullable = false, length = 50)
    private String senderId;

    @Column(name = "RECIPIENT_ID", nullable = false, length = 50)
    private String recipientId;

    @Column(name = "CONTENT", nullable = false, length = 1000)
    private String content;

    @Column(name = "SENT_AT", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "IS_READ", nullable = false)
    private boolean isRead;
}
