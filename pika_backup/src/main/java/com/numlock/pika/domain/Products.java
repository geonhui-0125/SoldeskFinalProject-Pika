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
@Builder
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq_gen")
    @SequenceGenerator(name = "products_seq_gen", sequenceName = "seq_products", allocationSize = 1)
    @Column(name = "product_id")
    private int productId; // 상품 고유 ID

    @OneToOne
    @JoinColumn(name = "seller_id")
    private Users seller; // 판매자 고유 ID

    @OneToOne
    @JoinColumn(name = "category_id")
    private Categories category; // 카테고리 고유 ID

    @Column(name = "price", nullable = false)
    private BigDecimal price; // 상품 가격

    @Column(name = "title", nullable = false)
    private String title; // 상품 제목

    @Column(name = "description", nullable = false)
    private String description; // 상품 설명

    @Column(name = "product_image", nullable = false)
    private String productImage; // 상품 섬네일

    @Column(name = "view_cnt")
    private int viewCnt; // 조회수

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성일

    @Column(name = "product_state")
    private int productState; // 0 : selling, 1 : selled

/*
    @Column(name = "policy_violation_status", nullable = false, length = 50)
    private String policyViolationStatus = "NORMAL"; // 운영 정책 위반 상태 (NORMAL, REPORTED, SUSPENDED, DELETED_BY_ADMIN)

    @Column(name = "policy_violation_reason")
    private String policyViolationReason; // 운영 정책 위반 사유 (null 허용)
*/

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}