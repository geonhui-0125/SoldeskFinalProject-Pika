package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "categories")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId; // 카테고리 고유 ID

    @Column(name = "category")
    private String category; // 카테고리 ex) 피규어>아크릴 스탠드 >를 구분자로 사용
}
