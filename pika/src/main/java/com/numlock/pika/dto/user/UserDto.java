package com.numlock.pika.dto.user;

import java.util.Date;

import com.numlock.pika.domain.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "아이디를 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
 	private String id;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "비밀번호는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
	private String pw;

	@NotBlank(message = "비밀번호를 다시 한 번 입력하세요.")
	@Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "비밀번호는 4~20자의 알파벳과 숫자만 사용 가능합니다.")
	private String confirmPw;

    @NotBlank(message = "닉네임을 입력하세요.")
	private String nickname;

    @NotBlank(message = "이메일 주소를 입력하세요.")
    @Email(message = "잘 못 된 이메일 형식입니다.")
	private String email;

	private String profileImage;
	private String address;
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
				.address(user.getAddress())
				.phone(user.getPhone())
				.birth(user.getBirth())
				.role(user.getRole())
				.build();
	}
}
