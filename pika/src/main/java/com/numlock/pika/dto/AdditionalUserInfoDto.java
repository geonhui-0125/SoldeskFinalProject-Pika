package com.numlock.pika.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalUserInfoDto {
    private String profileImage;
    private String address;
    private String phone;
    private String birth;
    private String role;

}
