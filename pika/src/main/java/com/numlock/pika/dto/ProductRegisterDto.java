package com.numlock.pika.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductRegisterDto {

    private String title;
    private String description;
    private BigDecimal price;

    // "전자기기 > 노트북" 형태
    private String category;
}
