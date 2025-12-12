package com.numlock.pika.service;

import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto getReviewById(Long reviewId);
    List<ReviewResponseDto> getReviewsByProductId(int productId);

    ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId);
    void deleteReview(Long reviewId, String currentUserId);

}
