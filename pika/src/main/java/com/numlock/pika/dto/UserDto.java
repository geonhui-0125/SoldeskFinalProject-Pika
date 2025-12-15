package com.numlock.pika.dto;

import java.util.Date;

import com.numlock.pika.domain.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserDto {
 	private String id;
	private String pw;
	private String nickname;
	private String email;
	private String profileImage;
	private String adress;
	private String phone;
	private Date birth;
	private String role;
	//Entity -> DTO
	public static UserDto fromEntity(Users user) {
		return UserDto.builder()
				.id(user.getId())
				.pw(user.getPw())
				.nickname(user.getNickname())
				.email(user.getEmail())
				.profileImage(user.getProfileImage())
				.adress(user.getAddress())
				.phone(user.getPhone())
				.birth(user.getBirth())
				.role(user.getRole())
				.build();
	}
}
