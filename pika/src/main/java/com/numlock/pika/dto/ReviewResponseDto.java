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
    private String sellerId;
    private String userId; // 리뷰 작성자 ID
    private Integer score;
    private String content;
}
