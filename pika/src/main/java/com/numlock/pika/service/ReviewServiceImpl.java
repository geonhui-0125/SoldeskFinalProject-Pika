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
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException import 추가
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

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // 권한 확인
        checkReviewAccess(review, currentUserId);

        review.update(reviewRequestDto.getScore(), reviewRequestDto.getContent()); // Reviews 엔티티의 update 메서드 사용
        Reviews updatedReview = reviewRepository.save(review); // 변경 감지(Dirty Checking)로 인해 save를 명시적으로 호출하지 않아도 되지만, 명확성을 위해 호출
        return mapToReviewResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // 권한 확인
        checkReviewAccess(review, currentUserId);

        reviewRepository.delete(review); // 물리적 삭제
    }

    // 리뷰 접근 권한 확인 헬퍼 메서드
    private void checkReviewAccess(Reviews review, String currentUserId) {
        Users currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        // 리뷰 작성자가 아니거나 관리자가 아닌 경우 접근 거부
        if (!review.getUser().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("You do not have permission to modify or delete this review.");
        }
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
