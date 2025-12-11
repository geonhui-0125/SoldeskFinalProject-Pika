package com.numlock.pika.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReviewResponseDto {
    private Long reviewId;
    private Long productId;
    private String userId;
    private Integer score;
}
