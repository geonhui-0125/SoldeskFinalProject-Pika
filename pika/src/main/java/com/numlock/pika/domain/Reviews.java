package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Table(name = "reviews")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_reviews")
    @SequenceGenerator(name = "seq_reviews", sequenceName = "seq_reviews", allocationSize = 1)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product; // 리뷰 상품 고유 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // 리뷰어 고유 ID

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content", length = 500) // 새로운 content 필드 추가
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정 일시

    public void update(Integer score, String content) {
        this.score = score;
        this.content = content;
    }
}