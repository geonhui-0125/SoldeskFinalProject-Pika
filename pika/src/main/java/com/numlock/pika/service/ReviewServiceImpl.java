package com.numlock.pika.service;

import com.numlock.pika.domain.Reviews;
import com.numlock.pika.domain.Products;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.repository.ReviewRepository;
import com.numlock.pika.repository.ProductRepository;
import com.numlock.pika.repository.UserRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        Products product = productRepository.findById(reviewRequestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + reviewRequestDto.getProductId()));
        Users user = userRepository.findById(reviewRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + reviewRequestDto.getUserId()));

        Reviews review = Reviews.builder()
                .product(product)
                .user(user)
                .score(reviewRequestDto.getScore())
                .content(reviewRequestDto.getContent())
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
    public List<ReviewResponseDto> getReviewsByProductId(int productId) {
        List<Reviews> reviews = reviewRepository.findByProduct_ProductId(productId);
        return reviews.stream()
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    private ReviewResponseDto mapToReviewResponseDto(Reviews review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProduct().getProductId())
                .userId(review.getUser().getId())
                .score(review.getScore())
                .content(review.getContent())
                .build();
    }
}
