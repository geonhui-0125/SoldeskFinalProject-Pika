package com.numlock.pika.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddInfoDto {
    private String profileImage;
    private String address;
    private String phone;
    private String birth;
    private String role;

}
