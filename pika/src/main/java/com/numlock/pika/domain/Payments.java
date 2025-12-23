package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "payments")
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Payments {

    @Id
    @Column(name = "imp_uid")
    private String impUid; // 포트원에서 보낸 결제 고유 ID

    @Column(name = "merchant_uid")
    private String merchantUid; // 포트원에서 보낸 판매자 고유 ID

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Products task; // 포트원에서 보낸 판매 상품 고유 ID

    @Column(name = "amount")
    private BigDecimal amount; // 포트원에서 보낸 결제 가격

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Users seller; // 포트원에서 보낸 구매자 고유 ID

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Users buyer; // 포트원에서 보낸 구매자 고유 ID

}
