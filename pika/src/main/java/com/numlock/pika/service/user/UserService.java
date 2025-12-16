package com.numlock.pika.service.user;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.AdditionalUserInfoDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;

    //정보 수정 처리
    public Users updateAddlInfo(String userId, AdditionalUserInfoDto dto, MultipartFile profileImage) throws IOException {
        Users user = userRepository.findById (userId)
                .orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //프로필 이미지 업데이트
        if(profileImage != null && !profileImage.isEmpty()){
            String profileImagePath = fileUploadService.storeImg(profileImage);
            user.setProfileImage(profileImagePath);
        }else if(dto.getProfileImage() != null && dto.getProfileImage().equals("default")){
            //"default" 가 DTO에서 넘어오면 기본이미지로 설정(ex. 사용자가 기존 이미지 삭제)
            user.setProfileImage("/profile/default-profile.jpg");
        }

        //주소 업데이트
        if(dto.getAddress() != null && !dto.getAddress().trim().isEmpty()){
            user.setAddress(dto.getAddress());
        }

        //전화번호 업데이트
        if(dto.getPhone()!= null && !dto.getPhone().trim().isEmpty()){
            //전화번호 유효성 검사
            if(!dto.getPhone().matches("^010-\\d{4}-\\d{4}$")){
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
            }
            user.setPhone(dto.getPhone());
        }

        //생년월일 업데이트
        if(dto.getBirth() != null && !dto.getBirth().trim().isEmpty()){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                sdf.setLenient(false);
                user.setBirth(sdf.parse(dto.getBirth()));
            } catch (ParseException e){
                throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다.");
            }
        }

        //모든 추가정보 입력시 role 변경
        boolean allInfoProvided = isAllAddInfoProvided(user);
        if(allInfoProvided && "GUEST".equals(user.getRole())){
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    //정보 입력 확인 메소드
    private boolean isAllAddInfoProvided(Users user){
        return user.getAddress() != null && !user.getAddress().trim().isEmpty() &&
                user.getPhone() != null && !user.getPhone().trim().isEmpty() &&
                user.getBirth() != null;
    }

    //비밀번호 재설정을 위한 아이디, 이메일 일치 확인
   public boolean checkUserByIdAndEmail(String id, String email){
        Optional<Users> userOptional = userRepository.findById(id);
        if(userOptional.isPresent()){
            Users user = userOptional.get();
            return user.getEmail().equalsIgnoreCase(email);
        }
        return false;
    }

    //비밀번호 재설정 메소드
    //public void passwordReset(String userId, S)

}
