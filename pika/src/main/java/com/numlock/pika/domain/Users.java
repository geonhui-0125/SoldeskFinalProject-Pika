package com.numlock.pika.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private String id;

    //필수 입력값
    @Column(nullable = false)
    private String pw;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String profileImage;
    @Column(nullable = false)
    private String email;

    //추가 입력값
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
