package com.numlock.pika.controller;

import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException; // 누락된 import 문 추가
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
    public String listReviewsByProductId(@PathVariable int productId, Model model) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("productId", productId); // 제품 ID를 뷰로 전달
        return "review/list"; // Thymeleaf 템플릿 가정: /templates/review/list.html
    }

    // 새 리뷰를 생성하기 위한 양식을 표시합니다.
    @GetMapping("/new/{productId}") // 특정 제품에 대한 리뷰 생성 링크
    public String createReviewForm(@PathVariable int productId, Model model) {
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
            reviewRequestDto.setUserId(principal.getName());
        } else {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            // 로그인 페이지로 리다이렉트하거나, 로그인 폼을 보여주는 등의 처리가 필요
            return "redirect:/login"; // 예시: 로그인 페이지로 리다이렉트
        }

        try {
            reviewService.createReview(reviewRequestDto);
            return "redirect:/reviews/product/" + reviewRequestDto.getProductId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "review/form";
        }
    }

    // 리뷰 수정 폼을 표시합니다.
    @GetMapping("/edit/{reviewId}")
    public String editReviewForm(@PathVariable Long reviewId, Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        ReviewResponseDto reviewResponse = reviewService.getReviewById(reviewId);
        // 현재 사용자 ID와 리뷰 작성자 ID, 관리자 여부를 서비스 계층에서 검증
        // 여기서는 간단히 reviewService.getReviewById()에서 예외 처리되거나,
        // 혹은 reviewService.getReviewById() 대신 권한 검증이 포함된 새로운 메서드를 사용할 수도 있습니다.
        // 현재는 서비스 계층에서 권한 검증이 이루어지므로, 그냥 조회 후 DTO를 생성해서 넘겨줍니다.

        // ReviewRequestDto로 변환하여 폼에 전달 (수정 필드만 포함)
        ReviewRequestDto reviewRequestDto = ReviewRequestDto.builder()
                .productId(reviewResponse.getProductId())
                .userId(reviewResponse.getUserId())
                .score(reviewResponse.getScore())
                .content(reviewResponse.getContent())
                .build();

        model.addAttribute("review", reviewRequestDto);
        model.addAttribute("reviewId", reviewId); // 폼 제출 시 reviewId를 전달하기 위함
        return "review/form"; // 기존 리뷰 생성 폼 재활용
    }

    // 리뷰 수정 양식 제출을 처리합니다.
    @PutMapping("/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                               @ModelAttribute("review") ReviewRequestDto reviewRequestDto,
                               Principal principal,
                               Model model) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            reviewService.updateReview(reviewId, reviewRequestDto, principal.getName());
            return "redirect:/reviews/product/" + reviewRequestDto.getProductId();
        } catch (AccessDeniedException e) {
            model.addAttribute("errorMessage", "리뷰 수정 권한이 없습니다.");
            model.addAttribute("review", reviewRequestDto); // 수정 폼에 데이터 유지
            model.addAttribute("reviewId", reviewId);
            return "review/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("review", reviewRequestDto);
            model.addAttribute("reviewId", reviewId);
            return "review/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("review", reviewRequestDto);
            model.addAttribute("reviewId", reviewId);
            return "review/form";
        }
    }

    // 리뷰 삭제를 처리합니다.
    @PostMapping("/{reviewId}/delete") // HTML 폼에서 DELETE 메서드를 직접 지원하지 않으므로 POST 사용
    public String deleteReview(@PathVariable Long reviewId, Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            // 삭제 후 리디렉션할 product_id를 얻기 위해 리뷰를 조회
            ReviewResponseDto reviewToDelete = reviewService.getReviewById(reviewId);
            reviewService.deleteReview(reviewId, principal.getName());
            return "redirect:/reviews/product/" + reviewToDelete.getProductId();
        } catch (AccessDeniedException e) {
            model.addAttribute("errorMessage", "리뷰 삭제 권한이 없습니다.");
            return "redirect:/reviews/" + reviewId; // 삭제 실패 시 해당 리뷰 상세 페이지로 돌아감
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404"; // 없는 리뷰 삭제 시도
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/reviews/" + reviewId;
        }
    }

    // 단일 리뷰의 세부 정보를 표시합니다.
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

