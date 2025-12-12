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
    private Integer productId;
    private String userId;
    private Integer score;
    private String content;
}
