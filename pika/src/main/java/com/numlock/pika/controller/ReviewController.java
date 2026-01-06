package com.numlock.pika.controller;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.ReviewRequestDto;
import com.numlock.pika.dto.ReviewResponseDto;
import com.numlock.pika.dto.SellerStatsDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.ReviewService;
import com.numlock.pika.service.GeminiService; // GeminiService import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException; // 누락된 import 문 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.numlock.pika.service.DuplicateReviewException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Collectors 임포트 추가

@Controller
@RequestMapping("/reviews") // 리뷰 관련 뷰의 기본 경로
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final GeminiService geminiService; // GeminiService 주입
    private final UserRepository userRepository;

    // 새 리뷰를 생성하기 위한 양식을 표시합니다.
    @GetMapping("/new") // 특정 판매자에 대한 리뷰 생성 링크
    public String createReviewForm(@RequestParam("productId") Integer productId,
                                   @RequestParam("sellerId") String sellerId,
                                   @RequestParam("sellerNickname") String sellerNickname,
                                   Principal principal,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        String userId = principal.getName();
        
        // 구매 여부 확인 (구매하지 않았으면 폼 접근 차단)
        if (!reviewService.hasUserPurchasedProduct(userId, productId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 구매한 분들만 리뷰를 남길 수 있습니다.");
            return "redirect:/products/info/" + productId;
        }

        ReviewRequestDto reviewRequestDto = new ReviewRequestDto();
        reviewRequestDto.setProductId(productId);
        reviewRequestDto.setSellerId(sellerId);
        reviewRequestDto.setUserId(principal.getName()); // 리뷰 작성자는 현재 로그인한 사용자
        
        model.addAttribute("review", reviewRequestDto);
        model.addAttribute("sellerNickname", sellerNickname); // 폼에 판매자 닉네임 표시용
        
        return "review/form"; // Thymeleaf 템플릿 가정: /templates/review/form.html
    }

    // 새 리뷰 양식 제출을 처리합니다.
    @PostMapping
    public String createReview(@ModelAttribute("review") ReviewRequestDto reviewRequestDto,
                               Principal principal, // 로그인한 사용자 정보를 가져오기 위해
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (principal != null) {
            reviewRequestDto.setUserId(principal.getName());
        } else {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 예시: 로그인 페이지로 리다이렉트
        }

        try {
            reviewService.createReview(reviewRequestDto);
            return "redirect:/products/info/" + reviewRequestDto.getProductId(); // 상품 정보 페이지로 리다이렉트
        } catch (DuplicateReviewException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/"; // 메인 페이지로 리다이렉트
        } catch (Exception e) {
            model.addAttribute("errorMessage", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return "review/form";
        }
    }

    // 리뷰 수정 폼을 표시합니다.
    @GetMapping("/edit/{reviewId}")
    public String editReviewForm(@PathVariable("reviewId") Long reviewId, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        String currentUserId = principal.getName();

        try {
            ReviewResponseDto review = reviewService.getReviewById(reviewId);

            // Check if the current user is the author of the review
            if (!review.getUserId().equals(currentUserId)) {
                // If not authorized, throw AccessDeniedException which will be caught below
                throw new AccessDeniedException("이 리뷰를 수정할 권한이 없습니다.");
            }

            // 판매자 닉네임 조회
            String sellerNickname = userRepository.findById(review.getSellerId())
                    .map(Users::getNickname)
                    .orElse("알 수 없는 판매자"); // 판매자를 찾지 못한 경우 기본값

            model.addAttribute("review", review);
            model.addAttribute("sellerNickname", sellerNickname); // 모델에 판매자 닉네임 추가
            // Assuming review/form.html can be reused for editing
            return "review/form";
        } catch (IllegalArgumentException e) {
            // Review not found
            model.addAttribute("errorMessage", "해당 리뷰를 찾을 수 없습니다.");
            return "error/404"; // Or redirect to a suitable error page
        } catch (AccessDeniedException e) {
            // User not authorized to edit this review
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/user/mypage"; // Redirect back to mypage with an error message
        } catch (Exception e) {
            // General error
            model.addAttribute("errorMessage", "리뷰 수정 중 오류가 발생했습니다.");
            return "redirect:/user/mypage"; // Redirect back to mypage with an error message
        }
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
            return "redirect:/user/mypage"; // 업데이트 후 마이페이지로 리다이렉트
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
            // 삭제 후 리디렉션할 sellerId를 얻기 위해 리뷰를 조회
            ReviewResponseDto reviewToDelete = reviewService.getReviewById(reviewId);
            reviewService.deleteReview(reviewId, principal.getName());
            return "redirect:/user/mypage"; // 삭제 후 마이페이지로 리다이렉트
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
    public String reviewDetail(@PathVariable Long reviewId, Principal principal, Model model) {
        try {
            ReviewResponseDto review = reviewService.getReviewById(reviewId);
            model.addAttribute("review", review);
            model.addAttribute("principal", principal); // 이 줄을 추가합니다.
            return "review/detail"; // Thymeleaf 템플릿 가정: /templates/review/detail.html
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404"; // 오류 페이지 가정
        }
    }

    // 특정 판매자에 대한 리뷰 요약 한줄평을 반환하는 API 엔드포인트
    @GetMapping("/summary/{sellerId}")
    @ResponseBody // 이 메서드가 뷰 이름을 반환하는 대신 직접 응답 본문을 반환하도록 지시합니다.
    public String getReviewSummaryForSeller(@PathVariable String sellerId) {
        // 1. ReviewService를 통해 해당 판매자의 모든 리뷰 내용을 가져옵니다.
        List<ReviewResponseDto> reviews = reviewService.getReviewsBySellerId(sellerId);

        // 2. ReviewResponseDto 리스트에서 리뷰 내용(content)만 추출하여 List<String>으로 변환합니다.
        List<String> reviewContents = reviews.stream()
                .map(ReviewResponseDto::getContent)
                .collect(Collectors.toList());

        // 3. GeminiService를 호출하여 리뷰 내용으로부터 한줄평을 생성합니다.
        String summary = geminiService.generateReviewSummary(reviewContents);

        // 4. 생성된 한줄평을 반환합니다.
        return summary;
    }
}
