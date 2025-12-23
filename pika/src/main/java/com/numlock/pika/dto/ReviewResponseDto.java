package com.numlock.pika.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReviewResponseDto {
    private Long reviewId;
    private String sellerId;
    private Integer productId;
    private String productName;
    private String userId; // 리뷰 작성자 ID
    private String profileImage; // 리뷰 작성자 프로필 이미지 경로 추가
    private Integer score;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
