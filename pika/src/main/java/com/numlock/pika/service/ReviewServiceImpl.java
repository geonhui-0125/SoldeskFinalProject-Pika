package com.numlock.pika.service;

import com.numlock.pika.domain.Reviews;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        Reviews review = Reviews.builder()
                .productId(reviewRequestDto.getProductId())
                .userId(reviewRequestDto.getUserId())
                .score(reviewRequestDto.getScore())
                .build();
        Reviews savedReview = reviewRepository.save(review);
        return mapToReviewResponseDto(savedReview);
    }

    @Override
    public ReviewResponseDto getReviewById(Long reviewId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));
        return mapToReviewResponseDto(review);
    }

    @Override
    public List<ReviewResponseDto> getReviewsByProductId(Long productId) {
        List<Reviews> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    private ReviewResponseDto mapToReviewResponseDto(Reviews review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .score(review.getScore())
                .build();
    }
}
