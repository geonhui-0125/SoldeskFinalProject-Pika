package com.numlock.pika.controller.payment;

import com.numlock.pika.dto.payment.PaymentResDto;
import com.numlock.pika.service.Notification.NotificationService;
import com.numlock.pika.service.payment.PaymentApiService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class PaymentApiController {

    // 결제 완료 후 서비스 로직
    private final PaymentApiService paymentApiService;
    private final NotificationService notificationService;

    // 결제 검증 controller
    @PostMapping("/api/payment/validation")
    public ResponseEntity<?> validateOrder(@RequestBody PaymentResDto paymentResDto) {

        System.out.println("paymentResDto = " + paymentResDto);

        try {

            // 서버 기대 금액 확인 ( 데이터 베이스의 상품 금액과 일치해야함)
            System.out.println("포트원으로부터 전달 받은 결제 금액 : " +  paymentResDto.getAmount());

            //PaymentService를 호출하여 PortOne API와 통신 및 금액 검증 수행
            IamportResponse<Payment> iamportResponse = paymentApiService.validatePayment(paymentResDto);
            Payment paymentData = iamportResponse.getResponse();

            //Paid : 결제 완료 상태
            System.out.println("결제 검증 성공! Paid 상태 : " + paymentData.getStatus());

            notificationService.sendSellerApproval(paymentResDto);

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

    //결제 확정 로직
    @PostMapping("api/payment/confirm/{impUid}")
    public ResponseEntity<?> confirmPayment(@PathVariable String impUid) {

        try {
            notificationService.sendSoldOut(impUid);
            notificationService.sendSellerSoldOut(impUid);
            notificationService.sendBuyerProductReview(impUid);

            String resultMessage = paymentApiService.confirmPayment(impUid);

            Map<String, String> response = new HashMap<>();
            response.put("message", resultMessage);

            return ResponseEntity.ok(response);

        } catch (IamportResponseException e) {
            System.out.println("PortOne API 통신 오류 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {
            System.out.println("IO 오류 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (RuntimeException e) {
            // 주로 PaymentService에서 금액 불일치 시 던지는 예외
            System.out.println("결제 확정 실패 : " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        }

    }

    @DeleteMapping("api/payments/cancel")
    public  String cancelPayment(@RequestBody PaymentResDto paymentResDto) {

        System.out.println("취소할 결제 - impUid : " + paymentResDto.getImpUid());

        int productId = paymentApiService.cancelPayment(paymentResDto.getImpUid());

        return "forward:/products/info/" + productId;
    }

    @PutMapping("api/payments/approve")
    public  String approvePayment(@RequestBody PaymentResDto paymentResDto) {

        notificationService.sendBuyerDealing(paymentResDto);

        System.out.println("승인할 결제 - impUid : " + paymentResDto.getTaskId());

        int productId = paymentApiService.approvePayment(paymentResDto.getTaskId());

        return "forward:/products/info/" + productId;
    }



}
