package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "products")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId; // 상품 고유 ID

    @Column(name = "seller_id")
    private String sellerId; // 판매자 고유 ID

    @Column(name = "category_id")
    private String categoryId; // 카테고리 고유 ID

    @Column(name = "price")
    private BigDecimal price; // 상품 가격

    @Column(name = "title")
    private String title; // 상품 제목

    @Column(name = "description")
    private String description; // 상품 설명

    @Column(name = "product_image")
    private String productImage; // 상품 섬네일

    @Column(name = "view_cnt")
    private int viewCnt; // 조회수

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성일

}
