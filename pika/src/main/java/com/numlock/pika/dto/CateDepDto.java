package com.numlock.pika.dto;

import lombok.*;

import java.util.List;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CateDepDto {

    private String depOne;
    private List<String> depOneTwo;
}
