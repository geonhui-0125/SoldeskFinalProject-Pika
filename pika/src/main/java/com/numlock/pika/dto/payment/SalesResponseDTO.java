package com.numlock.pika.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SalesResponseDTO {
    private String impUid;
    private BigDecimal amount;
    private LocalDateTime soldAt;

    private int productId;
    private String title;
    private String productImage;

    private int productState;      // 0, 1, 2
    private String statusText;     // "판매중", "판매완료", "거래중(확정대기)"

    private String buyerNickname;  // 나에게 산 사람
    private String buyerId;
}