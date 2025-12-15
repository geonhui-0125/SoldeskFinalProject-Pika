package com.numlock.pika.service;

import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.dto.SellerStatsDto;

import java.util.List;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto getReviewById(Long reviewId);
    List<ReviewResponseDto> getReviewsBySellerId(String sellerId);

    ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId);
    void deleteReview(Long reviewId, String currentUserId);
    SellerStatsDto getSellerStats(String sellerId);


}
