package com.numlock.pika.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SellerStatsDto {
    private Double averageRating;
    private Integer reviewCount;
}
