package com.numlock.pika.service;

import com.numlock.pika.domain.Products;
import com.numlock.pika.repository.ProductRepository;
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
import com.numlock.pika.repository.PaymentRepository; // PaymentRepository import 추가
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
    private final ProductRepository productRepository; // ProductRepository 주입
    private final PaymentRepository paymentRepository; // PaymentRepository 주입 추가
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // 상품 조회
        Products product = productRepository.findById(reviewRequestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + reviewRequestDto.getProductId()));
        // 판매자 조회 (리뷰 대상)
        Users seller = userRepository.findById(reviewRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with ID: " + reviewRequestDto.getSellerId()));
        // 리뷰 작성자 조회
        Users reviewer = userRepository.findById(reviewRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with ID: " + reviewRequestDto.getUserId()));

        // 판매자가 자신에게 리뷰를 작성하는 것을 방지
        if (seller.getId().equals(reviewer.getId())) {
            throw new IllegalArgumentException("자기 자신에게 리뷰를 작성할 수 없습니다");
        }

        // 구매 여부 확인
        boolean hasPurchased = paymentRepository.findByBuyerAndTask(reviewer, product).isPresent();
        if (!hasPurchased) {
            throw new IllegalArgumentException("상품을 구매한 사용자만 리뷰를 작성할 수 있습니다.");
        }

        // 특정 상품에 대해 동일한 사용자가 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByProductAndReviewer(product, reviewer)) {
            throw new DuplicateReviewException("이미 리뷰를 작성하였어요.");
        }

        Reviews review = Reviews.builder()
                .product(product) // Product 엔티티 연결
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
    public List<ReviewResponseDto> getReviewsByWriterId(String writerId) {
        List<Reviews> reviews = reviewRepository.findByReviewer_Id(writerId);
        return reviews.stream()
                .map(this::mapToReviewResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        checkReviewAuthor(review, currentUserId); // Call the new specific method

        review.update(reviewRequestDto.getScore(), reviewRequestDto.getContent());
        Reviews updatedReview = reviewRepository.save(review);
        return mapToReviewResponseDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String currentUserId) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        checkReviewAuthorOrAdmin(review, currentUserId);

        reviewRepository.delete(review);
    }

    // 리뷰 수정/삭제 시 작성자 본인 또는 ADMIN만 허용하는 로직 (삭제에 사용)
    private void checkReviewAuthorOrAdmin(Reviews review, String currentUserId) {
        Users currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Current user not found"));

        if (!review.getReviewer().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) {
            throw new AccessDeniedException("You do not have permission to modify or delete this review.");
        }
    }

    // 리뷰 수정 시 작성자 본인만 허용하는 로직 (수정에 사용)
    private void checkReviewAuthor(Reviews review, String currentUserId) {
        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to modify this review.");
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

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(String userId, int productId) {
        return reviewRepository.existsByReviewer_IdAndProduct_ProductId(userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProduct(String userId, int productId) {
        Users user = userRepository.findById(userId).orElse(null);
        Products product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null) return false;
        return paymentRepository.findByBuyerAndTask(user, product).isPresent();
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
                .productId(review.getProduct().getProductId()) // productId 추가
                .productName(review.getProduct().getTitle()) // productName 추가
                .sellerId(review.getSeller().getId())
                .userId(freshReviewer.getId())
                .profileImage(freshReviewer.getProfileImage() != null ? freshReviewer.getProfileImage() : "/profile/default-profile.png")
                .score(review.getScore())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
        
        logger.debug("ReviewResponseDto Profile Image: {}", dto.getProfileImage());
        return dto;
    }

}
