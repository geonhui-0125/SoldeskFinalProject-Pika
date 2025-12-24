package com.numlock.pika.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.time.LocalDateTime; // LocalDateTime 임포트 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@ToString
public class Users {

    //PK
    @Id
    @Column(nullable = false)
    private String id;

    //필수 입력값
    @Column(nullable = false, length = 255)
    private String pw;

    @Column(nullable = false)
    private String nickname;


    @Column(nullable = false)
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

/*
    @Column(nullable = false)
    private Integer warningCount = 0; // 경고 횟수

    @Column(nullable = false)
    private Boolean isRestricted = false; // 이용 제한 여부

    private String restrictionReason; // 이용 제한 사유 (null 허용)

    @DateTimeFormat(pattern = "yyyyMMddHHmmss") // 날짜 및 시간 형식 지정
    private LocalDateTime restrictionEndDate; // 이용 제한 종료일 (null 허용)
*/

    @Builder
    public Users(String id, String pw, String nickname, String profileImage, String email, String role) {
        this.id = id;
        this.pw = pw;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.email = email;
        this.role = role;
    }
}
