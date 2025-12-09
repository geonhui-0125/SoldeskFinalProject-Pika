package com.numlock.pika.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    private String password;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String profileImage;
    @Column(nullable = false)
    private String email;

    //추가 입력값
    private String address;
    private String phone;
    //private int birth_date;

    //로그인 타입 분류
    @Column(nullable = false)
    private String role;

    @Builder
    public Users(String id, String password, String nickname, String profileImage, String email, String role) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.email = email;
        this.role = role;
    }

    public Users updateUserRole(String address, String phone){
        this.address = address;
        this.phone = phone;
        this.role = "USER";
        return this;
    }

}
