package com.numlock.pika.service;

import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto getReviewById(Long reviewId);
    List<ReviewResponseDto> getReviewsByProductId(int productId);
    // Potentially add methods for update and delete later
}
