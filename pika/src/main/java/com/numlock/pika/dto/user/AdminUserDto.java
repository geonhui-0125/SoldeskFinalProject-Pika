package com.numlock.pika.dto.user;

import com.numlock.pika.domain.Users;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AdminUserDto {
    private String id;
    private String nickname;
    private String email;
    private String profileImage;
    private String address;
    private String phone;
    private Date birth;
    private String role;
    // private Integer warningCount;
    // private Boolean isRestricted;
    // private String restrictionReason;
    // private LocalDateTime restrictionEndDate;

    // Entity -> DTO
    public static AdminUserDto fromEntity(Users user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .address(user.getAddress())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .role(user.getRole())
                // .warningCount(user.getWarningCount())
                // .isRestricted(user.getIsRestricted())
                // .restrictionReason(user.getRestrictionReason())
                // .restrictionEndDate(user.getRestrictionEndDate())
                .build();
    }
}
