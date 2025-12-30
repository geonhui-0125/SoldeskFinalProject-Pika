package com.numlock.pika.dto.user;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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
    private String detailAddress;

    private String phone;

    @Pattern(regexp = "^$|^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$",
            message = "올바른 생년월일을 입력하세요.")
    private String birth;

    private String role;

}
