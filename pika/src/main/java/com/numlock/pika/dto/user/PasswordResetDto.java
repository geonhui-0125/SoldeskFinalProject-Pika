package com.numlock.pika.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PasswordResetDto {
    private String userId;
    private String token;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "비밀번호는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
    private String newPw;

    @NotBlank(message = "비밀번호를 다시 입력하세요.")
    private String confirmNewPw;
}
