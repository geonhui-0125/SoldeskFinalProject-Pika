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
public class PaymentResDto {

    //결제 검증 파라미터 정의
    private String impUid; //포트원
    private String merchantUid; //가맹점 주문번호 결제 요청마다 고유하게 지정
    private int taskId; //결제 상품의 고유 ID
    private BigDecimal amount; //결제금액
    private String buyerId; // 구매자 고유 ID
    
}
