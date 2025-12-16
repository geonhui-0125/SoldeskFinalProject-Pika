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
    @JoinColumn(name = "seller_id")
    private Users seller; // 리뷰 대상 판매자 ID

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Users reviewer; // 리뷰 작성자 ID

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content", length = 300, nullable = false) // 새로운 content 필드 추가
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