package com.numlock.pika.dto;

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
public class PaymentDto {

    //결제요청 파라미터 정의
    private String pg; //PG사 구분코드
    private String py_method; //결제수단 구분코드
    private String merchantUid; //가맹점 주문번호 결제 요청마다 고유하게 지정
    private String name; //결제대상 제품명
    private String impUid; //포트원에서 받은 가맹점 고유 UID - 결제 api 요청시 전달
    private int taskId; //결제 대상의 고유 ID(상품 ID)
    private BigDecimal amount; //결제금액

    //주문자 정보 front단에서 작성 후 바로 전달
    /*private String buyer_name; //주문자명
    private String buyer_tel; //주문자 연락처
    private String buyer_email; //주문자 이메일
    private String buyer_addr; //주문자 주소*/

}
