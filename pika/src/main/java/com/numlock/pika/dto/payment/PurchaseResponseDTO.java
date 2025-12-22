package com.numlock.pika.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PurchaseResponseDTO {
    private String impUid;
    private BigDecimal amount;
    private LocalDateTime purchasedAt;

    private int productId;
    private String title;
    private String productImage;

    private int productState;      // 0, 1, 2
    private String statusText;     // "판매중", "판매완료", "거래중(확정대기)"

    private String sellerNickname; // 나에게 판 사람
    private String sellerId;
}