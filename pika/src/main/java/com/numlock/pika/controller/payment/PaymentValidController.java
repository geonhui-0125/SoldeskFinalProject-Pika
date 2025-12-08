package com.numlock.pika.controller.payment;

import com.numlock.pika.dto.PaymentValidDto;
import com.numlock.pika.service.payment.PaymentValidService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class PaymentValidController {

    private final PaymentValidService paymentVaildService;

    @PostMapping("/api/payments/validation")
    public ResponseEntity<?> validateOrder(@RequestBody PaymentValidDto paymentVaildDto) {

        System.out.println("paymentVaildDto = " + paymentVaildDto);

        try {

            // 서버 기대 금액 확인 ( 데이터 베이스의 상품 금액과 일치해야함)
            System.out.println("포트원으로부터 전달 받은 결제 금액 : " +  paymentVaildDto.getAmount());

            //PaymentService를 호출하여 PortOne API와 통신 및 금액 검증 수행
            IamportResponse<Payment> iamportResponse = paymentVaildService.validatePayment(paymentVaildDto);

            // 3. 결제 상태에 따른 추가 처리 (DB 저장, 재고 차감 등)
            Payment paymentData = iamportResponse.getResponse();

            System.out.println("결제 검증 성공! Paid 상태 : " + paymentData.getStatus());

            return ResponseEntity.ok(iamportResponse);

        } catch (IamportResponseException e) {
            System.out.println("PortOne API 통신 오류 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {
            System.out.println("IO 오류 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (RuntimeException e) {
            // 주로 PaymentService에서 금액 불일치 시 던지는 예외
            System.out.println("금액/유효성 검증 실패 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        }
    }

}
