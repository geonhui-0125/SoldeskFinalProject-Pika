package com.numlock.pika.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@ToString
public class Users {

    //PK
    @Id
    @Column(nullable = false)
    @NotBlank(message = "아이디를 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
    private String id;

    //필수 입력값
    @Column(nullable = false)
    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "비밀번호는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
    private String pw;

    @Column(nullable = false)
    @NotBlank(message = "닉네임을 입력하세요.")
    private String nickname;


    @Column(nullable = false)
    @NotBlank(message = "이메일 주소를 입력하세요.")
    @Email(message = "잘 못 된 이메일 형식입니다.")
    private String email;

    //추가 입력값
    @Column(nullable = false)
    private String profileImage;

    private String address;

    private String phone;

    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date birth;

    //로그인 타입 분류
    @Column(nullable = false)
    private String role;

    @Builder
    public Users(String id, String pw, String nickname, String profileImage, String email, String role) {
        this.id = id;
        this.pw = pw;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.email = email;
        this.role = role;
    }

    public Users updateUserRole(String address, String phone, Date birth){
        this.address = address;
        this.phone = phone;
        this.birth = birth;
        this.role = "USER";
        return this;
    }

}
