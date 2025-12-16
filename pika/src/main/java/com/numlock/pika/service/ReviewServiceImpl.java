package com.numlock.pika.service;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.numlock.pika.domain.Reviews;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.dto.SellerStatsDto;
import com.numlock.pika.repository.ReviewRepository;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // 판매자 조회 (리뷰 대상)
        Users seller = userRepository.findById(reviewRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with ID: " + reviewRequestDto.getSellerId()));
        // 리뷰 작성자 조회
        Users reviewer = userRepository.findById(reviewRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + reviewRequestDto.getUserId()));

        Reviews review = Reviews.builder()
                .seller(seller)
                .reviewer(reviewer)
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
    public List<ReviewResponseDto> getReviewsBySellerId(String sellerId) {
        List<Reviews> reviews = reviewRepository.findBySeller_Id(sellerId);
        return reviews.stream()
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        checkReviewAccess(review, currentUserId);

        review.update(reviewRequestDto.getScore(), reviewRequestDto.getContent());
        Reviews updatedReview = reviewRepository.save(review);
        return mapToReviewResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        checkReviewAccess(review, currentUserId);

        reviewRepository.delete(review);
    }

    private void checkReviewAccess(Reviews review, String currentUserId) {
        Users currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        if (!review.getReviewer().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("You do not have permission to modify or delete this review.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SellerStatsDto getSellerStats(String sellerId) {
        List<Reviews> sellerReviews = reviewRepository.findBySeller_Id(sellerId);

        double averageRating = sellerReviews.stream()
                .mapToInt(Reviews::getScore)
                .average()
                .orElse(0.0);

        int reviewCount = sellerReviews.size();

        return SellerStatsDto.builder()
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();
    }

    private ReviewResponseDto mapToReviewResponseDto(Reviews review) {
        Users reviewer = review.getReviewer();
        // Detach the potentially stale reviewer entity from the persistence context
        if (entityManager.contains(reviewer)) { // Check if it's managed first
            entityManager.detach(reviewer);
        }
        // Now, fetch a fresh copy from the database using its ID
        Users freshReviewer = userRepository.findById(reviewer.getId())
                                 .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + reviewer.getId()));

        logger.debug("Reviewer ID: {}", freshReviewer.getId());
        logger.debug("Reviewer Profile Image from DB (after refresh): {}", freshReviewer.getProfileImage());

        ReviewResponseDto dto = ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .sellerId(review.getSeller().getId())
                .userId(freshReviewer.getId())
                .profileImage(freshReviewer.getProfileImage() != null ? freshReviewer.getProfileImage() : "/profile/default-profile.jpg")
                .score(review.getScore())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
        
        logger.debug("ReviewResponseDto Profile Image: {}", dto.getProfileImage());
        return dto;
    }
}
