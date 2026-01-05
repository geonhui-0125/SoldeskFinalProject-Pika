package com.numlock.pika.service.user;

import com.numlock.pika.config.JwtUtil;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.user.AdditionalUserInfoDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.file.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSenderImpl mailSender;

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
            user.setProfileImage("/profile/default-profile.png");
        }

        //주소 업데이트
        if(dto.getAddress() != null && !dto.getAddress().trim().isEmpty()){
            String fullAddress = dto.getAddress().trim();
            if(dto.getDetailAddress() != null && !dto.getDetailAddress().trim().isEmpty()){
                fullAddress += " " + dto.getDetailAddress().trim();
            }
            user.setAddress(fullAddress);
        } else {
            user.setAddress(null);
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
        if(dto.getBirth() != null && !dto.getBirth().isEmpty()){
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
                sdf.setLenient(false);
                user.setBirth(sdf.parse(dto.getBirth()));
            } catch (java.text.ParseException e) {
                throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다.");
            }
        }else{
            user.setBirth(null);
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

    public Users findById(String UserId){
        return userRepository.findById(UserId).orElse(null);
    }
    public List<Users> findAll(){
        Iterable<Users> iterable = userRepository.findAll();
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

    //비밀번호 재설정 이메일 처리 (JWT 토큰 발송)
    public boolean handlePasswordResetRequest(String id, String email, HttpServletRequest request){
        Optional<Users> userOptional = userRepository.findByIdAndEmail(id, email);
        if(userOptional.isPresent()){
            Users user = userOptional.get();
            String token = jwtUtil.generateToken(user.getId());

            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(null)
                    .build()
                    .toUriString();

            String resetLink = baseUrl + "/user/password/reset-form?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[PIKA] 비밀번호 재설정을 위한 안내 메일입니다.");

            StringBuilder mailContent = new StringBuilder();
            mailContent.append("안녕하세요, PIKA 서비스 운영팀입니다.\n\n");
            mailContent.append("귀하의 계정에 대한 비밀번호 재설정 요청이 접수되었습니다.\n");
            mailContent.append("비밀번호 변경을 원하실 경우, 아래의 링크를 클릭하여 절차를 진행해 주시기 바랍니다.\n\n");
            mailContent.append("[비밀번호 재설정 링크]\n");
            mailContent.append(resetLink).append("\n\n");
            mailContent.append("※ 해당 링크는 보안을 위해 요청 후 약 ");
            mailContent.append(jwtUtil.getExpirationMs() / 60000);
            mailContent.append("분 동안만 유효합니다.\n");
            mailContent.append("만약 본인이 요청한 것이 아니라면, 본 메일을 무시하시거나 고객센터로 문의해 주세요.\n\n");
            mailContent.append("감사합니다.\nPIKA 운영팀 드림");

            message.setText(mailContent.toString());
            /*SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[PIKA] 비밀번호 재설정 인증 메일입니다.");
            message.setText("pika 비밀번호 재설정 안내 메일입니다. \n\n 유효시간 내에 아래 링크로 접속해주세요. \n\n"+
                    resetLink + "\n\n이 링크는 "+ (jwtUtil.getExpirationMs() / 60000) +"분 동안 유효합니다." );*/
            mailSender.send(message);
            return true;
        }
        return false;
    }

    public boolean resetPassword(String token, String newPassword){
        if(jwtUtil.validateToken(token)){
            String userId = jwtUtil.getUserIdFromToken(token);
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            user.setPw(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
