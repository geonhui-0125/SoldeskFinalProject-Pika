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
@Builder
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_reviews")
    @SequenceGenerator(name = "seq_reviews", sequenceName = "seq_reviews", allocationSize = 1)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "score", nullable = false)
    private Integer score;
}
