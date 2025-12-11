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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // 찜한 유저 ID

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product; // 찜한 상품 ID
}
