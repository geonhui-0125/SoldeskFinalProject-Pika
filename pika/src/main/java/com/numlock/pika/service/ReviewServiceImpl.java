package com.numlock.pika.service;

import com.numlock.pika.domain.Reviews;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.repository.ReviewRepository;
import com.numlock.pika.repository.UserRepository; // ProductRepository는 더 이상 필요 없음
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

    private final ReviewRepository reviewRepository;
    // private final ProductRepository productRepository; // ProductRepository는 더 이상 필요 없음
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // 판매자 조회 (리뷰 대상)
        Users seller = userRepository.findById(reviewRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with ID: " + reviewRequestDto.getSellerId()));
        // 리뷰 작성자 조회
        Users reviewer = userRepository.findById(reviewRequestDto.getUserId()) // reviewRequestDto의 userId는 리뷰 작성자 ID
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + reviewRequestDto.getUserId()));

        Reviews review = Reviews.builder()
                .seller(seller) // 상품 대신 판매자 설정
                .reviewer(reviewer) // 리뷰 작성자 설정
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
    public List<ReviewResponseDto> getReviewsBySellerId(String sellerId) { // 메서드 시그니처 변경
        List<Reviews> reviews = reviewRepository.findBySeller_Id(sellerId); // 레포지토리 메서드 변경
        return reviews.stream()
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        // 권한 확인 (리뷰 작성자 또는 관리자)
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

        // 권한 확인 (리뷰 작성자 또는 관리자)
        checkReviewAccess(review, currentUserId);

        reviewRepository.delete(review);
    }

    // 리뷰 접근 권한 확인 헬퍼 메서드
    private void checkReviewAccess(Reviews review, String currentUserId) {
        Users currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        // 리뷰 작성자가 아니거나 관리자가 아닌 경우 접근 거부
        if (!review.getReviewer().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) { // review.getUser() 대신 review.getReviewer() 사용
            throw new AccessDeniedException("You do not have permission to modify or delete this review.");
        }
    }

    private ReviewResponseDto mapToReviewResponseDto(Reviews review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .sellerId(review.getSeller().getId()) // productId 대신 sellerId 사용
                .userId(review.getReviewer().getId()) // user ID 대신 reviewer ID 사용 (리뷰 작성자)
                .score(review.getScore())
                .content(review.getContent())
                .build();
    }
}
