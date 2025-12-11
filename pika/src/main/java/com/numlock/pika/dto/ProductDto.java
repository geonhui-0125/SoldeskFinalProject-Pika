package com.numlock.pika.dto;

import com.numlock.pika.domain.Products;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProductDto {
    private int productId;
    private String sellerId;
    private int categoryId;
    private BigDecimal price;
    private String title;
    private String description;
    private String productImage;
    private int viewCnt;
    private LocalDateTime createdAt;
    private int productState;

    // Entity -> DTO
    public static ProductDto fromEntity(Products product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .categoryId(product.getCategoryId())
                .price(product.getPrice())
                .title(product.getTitle())
                .description(product.getDescription())
                .productImage(product.getProductImage())
                .viewCnt(product.getViewCnt())
                .createdAt(product.getCreatedAt())
                .productState(product.getProductState())
                .build();
    }

    // DTO -> Entity
    public Products toEntity() {
        return Products.builder()
                .productId(productId)
                .sellerId(sellerId)
                .categoryId(categoryId)
                .price(price)
                .title(title)
                .description(description)
                .productImage(productImage)
                .viewCnt(viewCnt)
                .createdAt(createdAt)
                .productState(productState)
                .build();
    }
}
