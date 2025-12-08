package com.numlock.pika.controller.payment;

import com.numlock.pika.dto.PaymentDto;
import com.numlock.pika.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class PaymentController {

    // 상품 결제 정보 조회 서비스
    private final PaymentService paymentService;

    // 상품 구매 버튼을 누를 시 결제 페이지로 전달할 결제 요청
    @GetMapping("/payments/info")
    public String getPaymentInfo(@RequestParam("productId") int id, Model model) {

        // 상품 결제 정보 조회 서비스에 상품 id 전달
        PaymentDto paymentDto = paymentService.getPayemntInfo(id);
        // 모델에 담기
        model.addAttribute("paymentDto", paymentDto);

        return "payment/payment";
    }

    //결제 결과 화면 이동
    @GetMapping("/payment/result")
    public String getPaymentResult(@RequestParam("productId") int id, Model model) {


        return  "payment/result";
    }

    //결제 확정 로직
    @GetMapping("/payment/confirm")
    public String confirmPayment(@RequestParam("impUid") String impUid, Model model) {

        paymentService.confirmPayment(impUid);

        return "product/main"; //추후 수정
    }

}
