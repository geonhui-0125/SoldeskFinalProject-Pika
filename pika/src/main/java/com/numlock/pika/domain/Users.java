package com.numlock.pika.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String id; // 유저 ID

    @Column(name = "pw")
    private String pw; // 유저 PW

    @Column(name = "nickname")
    private String nickname; // 닉네임

    @Column(name = "profile_image")
    private String profileImage; // 유저 프로필 이미지

    @Column(name = "address")
    private String address; // 주소

    @Column(name = "phone")
    private String phone; // 전화번호

    @Column(name = "birth")
    private LocalDateTime birth; // 생년월일

}
