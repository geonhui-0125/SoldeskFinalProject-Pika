package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "payments")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imp_uid")
    private String impUid; // 포트원에서 보낸 결제 고유 ID

    @Column(name = "merchant_uid")
    private String merchantUid; // 포트원에서 보낸 판매자 고유 ID

    @Column(name = "task_id")
    private int taskId; // 포트원에서 보낸 판매 상품 고유 ID

    @Column(name = "amount")
    private int amount; // 포트원에서 보낸 결제 가격

}
