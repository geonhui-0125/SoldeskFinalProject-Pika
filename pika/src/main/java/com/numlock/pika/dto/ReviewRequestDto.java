package com.numlock.pika.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReviewRequestDto {
    private String sellerId;
    private String userId;
    private Integer score;
    private String content;
    private Integer productId;
}
