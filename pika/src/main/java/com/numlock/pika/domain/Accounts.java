package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "accounts")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Accounts {

    @SequenceGenerator(
            name = "accounts_seq_generator",
            sequenceName = "SEQ_ACCOUNTS",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "accounts_seq_generator"
    )
    @Column(name = "account_id")
    private int accountId; // 계좌 고유 ID

    @OneToOne
    @JoinColumn(name = "seller_id")
    private Users seller; // 계좌 소유자(판매자)

    @Column(name = "bank_code")
    private String bankCode; // 은행 고유 코드 ex) 국민 - 88

    @Column(name = "account_number")
    private String accountNumber; // 계좌 번호

}
