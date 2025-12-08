package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "favorite_products")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FavoriteProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fp_id")
    private int fpId; // 찜 고유 ID

    @Column(name = "user_id")
    private String userId; // 찜한 유저 ID

    @Column(name = "product_id")
    private int productId; // 찜한 상품 ID
}
