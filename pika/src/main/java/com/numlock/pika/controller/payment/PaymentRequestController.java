package com.numlock.pika.controller.payment;

import com.numlock.pika.dto.payment.PaymentRequestDto;
import com.numlock.pika.service.payment.PaymentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class PaymentRequestController {

    // 상품 결제 정보 조회 서비스
    private final PaymentRequestService paymentRequestService;

    // 상품 구매 버튼을 누를 시 결제 페이지로 전달할 결제 요청
    @GetMapping("/payments/preview")
    public String getPaymentInfo(@RequestParam("productId") int productId, Principal principal, Model model) {

        // 상품 결제 정보 조회 서비스에 상품 id 전달
        PaymentRequestDto paymentRequestDto = paymentRequestService.getPaymentPreview(productId, principal);
        // 모델에 담기
        model.addAttribute("paymentRequestDto", paymentRequestDto);
        // 상품 결제 미리보기 페이지 리턴
        return "payment/preview";
    }

}