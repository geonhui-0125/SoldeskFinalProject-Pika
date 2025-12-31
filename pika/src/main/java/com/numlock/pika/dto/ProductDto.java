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
    private boolean hasChat;

    // Entity -> DTO
    public static ProductDto fromEntity(Products product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .sellerId(product.getSeller().getId())
                .categoryId(product.getCategory().getCategoryId())
                .price(product.getPrice())
                .title(product.getTitle())
                .description(product.getDescription())
                .productImage(product.getProductImage())
                .viewCnt(product.getViewCnt())
                .createdAt(product.getCreatedAt())
                .productState(product.getProductState())
                .build();
    }

}
