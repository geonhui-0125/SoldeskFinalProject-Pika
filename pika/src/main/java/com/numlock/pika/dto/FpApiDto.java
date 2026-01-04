package com.numlock.pika.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FpApiDto {

    private int productId;
    private String productImage;
    private int fpCnt;

}

