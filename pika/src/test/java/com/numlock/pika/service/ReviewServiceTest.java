package com.numlock.pika.service;

import com.numlock.pika.domain.Reviews;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Reviews review;
    private ReviewRequestDto reviewRequestDto;
    private ReviewResponseDto reviewResponseDto;

    @BeforeEach
    void setUp() {
        review = Reviews.builder()
                .reviewId(1L)
                .productId(100L)
                .userId("testUser")
                .score(5)
                .build();

        reviewRequestDto = ReviewRequestDto.builder()
                .productId(100L)
                .userId("testUser")
                .score(5)
                .build();

        reviewResponseDto = ReviewResponseDto.builder()
                .reviewId(1L)
                .productId(100L)
                .userId("testUser")
                .score(5)
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 테스트")
    void createReview_ShouldReturnReviewResponseDto() {
        // Given
        when(reviewRepository.save(any(Reviews.class))).thenReturn(review);

        // When
        ReviewResponseDto result = reviewService.createReview(reviewRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(review.getReviewId());
        assertThat(result.getProductId()).isEqualTo(review.getProductId());
        assertThat(result.getUserId()).isEqualTo(review.getUserId());
        assertThat(result.getScore()).isEqualTo(review.getScore());
        verify(reviewRepository, times(1)).save(any(Reviews.class));
    }

    @Test
    @DisplayName("ID로 리뷰 조회 테스트 - 성공")
    void getReviewById_ShouldReturnReviewResponseDto_WhenReviewExists() {
        // Given
        when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.of(review));

        // When
        ReviewResponseDto result = reviewService.getReviewById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(review.getReviewId());
        verify(reviewRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("ID로 리뷰 조회 테스트 - 실패 (리뷰 없음)")
    void getReviewById_ShouldThrowException_WhenReviewDoesNotExist() {
        // Given
        when(reviewRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> reviewService.getReviewById(99L));
        verify(reviewRepository, times(1)).findById(any(Long.class));
    }

    @Test
    @DisplayName("제품 ID로 리뷰 목록 조회 테스트")
    void getReviewsByProductId_ShouldReturnListOfReviewResponseDto() {
        // Given
        List<Reviews> reviewsList = Arrays.asList(review, Reviews.builder().reviewId(2L).productId(100L).userId("anotherUser").score(4).build());
        when(reviewRepository.findByProduct_ProductId(any(int.class))).thenReturn(reviewsList);

        // When
        List<ReviewResponseDto> result = reviewService.getReviewsByProductId(100);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductId()).isEqualTo(100L);
        assertThat(result.get(1).getProductId()).isEqualTo(100L);
        verify(reviewRepository, times(1)).findByProductId(any(Long.class));
    }
}
