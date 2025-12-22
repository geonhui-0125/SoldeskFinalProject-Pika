package com.numlock.pika.dto.payment;

import lombok.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
@ToString
public class PaymentRequestDto {

    //결제요청 파라미터 정의
    private String impCode; // 포트원에서 받은 앱 식별 고유 UID - 결제 api 요청시 전달
    private String merchantUid; //가맹점(판매자) 고유 번호 결제 !!요청마다 고유하게 지정
    private String productTitle; // 결제대상 제품명
    private String productCategory; // 결제대상 카테고리
    private int productId; // 결제 대상의 고유 ID(상품 ID)
    private BigDecimal amount; //결제금액

    //주문자 정보 front단에서 작성 후 바로 전달
    private String buyerId; //주문자 고유 ID
    private String buyerName; //주문자명 nickname
    private String buyerTel; //주문자 연락처
    private String buyerEmail; //주문자 이메일
    private String buyerAddr; //주문자 주소

}
