package com.numlock.pika.dto;

import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalUserInfoDto {
    private String profileImage;
    private String address;
    private String phone;

    @DateTimeFormat(pattern = "yyyyMMdd")
    @PastOrPresent(message = "올바른 생년월일을 입력하세요.")
    private java.util.Date birth;

    private String role;

}
