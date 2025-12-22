package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "favorite_products")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FavoriteProducts {

    @SequenceGenerator(
            name = "favorite_product_seq_generator", // JPA 내부에서 참조할 별칭 (자유롭게 지정 가능)
            sequenceName = "SEQ_FAVORITE_PRODUCTS",  // 🌟 데이터베이스의 실제 시퀀스 이름 입력
            allocationSize = 1 // DB 시퀀스 정의에 따라 설정 (기본값 1 또는 50 사용)
    )

    // 2. 정의된 시퀀스 생성기를 @GeneratedValue에서 참조
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "favorite_product_seq_generator" // 위 @SequenceGenerator의 name과 일치해야 함
    )
    @Column(name = "fp_id")
    private int fpId; // 찜 고유 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user; // 찜한 유저 ID

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products product; // 찜한 상품 ID
}
