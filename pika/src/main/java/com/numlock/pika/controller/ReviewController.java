package com.numlock.pika.controller;

import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/reviews") // 리뷰 관련 뷰의 기본 경로
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 특정 제품에 대한 리뷰 목록을 표시합니다.
    @GetMapping("/product/{productId}")
    public String listReviewsByProductId(@PathVariable Long productId, Model model) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("productId", productId); // 제품 ID를 뷰로 전달
        return "review/list"; // Thymeleaf 템플릿 가정: /templates/review/list.html
    }

    // 새 리뷰를 생성하기 위한 양식을 표시합니다.
    @GetMapping("/new/{productId}") // 특정 제품에 대한 리뷰 생성 링크
    public String createReviewForm(@PathVariable Long productId, Model model) {
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto();
        reviewRequestDto.setProductId(productId); // 제품 ID를 미리 채웁니다.
        model.addAttribute("review", reviewRequestDto);
        return "review/form"; // Thymeleaf 템플릿 가정: /templates/review/form.html
    }

    // 새 리뷰 양식 제출을 처리합니다.
    @PostMapping
    public String createReview(@ModelAttribute("review") ReviewRequestDto reviewRequestDto,
                               Principal principal, // 로그인한 사용자 정보를 가져오기 위해
                               Model model) {
        if (principal != null) {
            reviewRequestDto.setUserId(principal.getName()); // 로그인한 사용자 ID 설정
        } else {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "review/form"; // 오류 메시지와 함께 양식으로 돌아갑니다.
        }

        try {
            reviewService.createReview(reviewRequestDto);
            // 제품의 리뷰 목록 또는 제품 상세 페이지로 리디렉션합니다.
            return "redirect:/reviews/product/" + reviewRequestDto.getProductId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "review/form"; // 오류 메시지와 함께 양식으로 돌아갑니다.
        }
    }

    // 단일 리뷰의 세부 정보를 표시합니다. (선택 사항)
    @GetMapping("/{reviewId}")
    public String reviewDetail(@PathVariable Long reviewId, Model model) {
        try {
            ReviewResponseDto review = reviewService.getReviewById(reviewId);
            model.addAttribute("review", review);
            return "review/detail"; // Thymeleaf 템플릿 가정: /templates/review/detail.html
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404"; // 오류 페이지 가정
        }
    }
}

