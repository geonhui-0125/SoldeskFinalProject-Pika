package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "reviews")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId; // 리뷰 고유 ID

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product; // 리뷰 상품 고유 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // 리뷰어 고유 ID

    @Column(name = "score")
    private int score; // 별점 1 ~ 5
}
