package com.numlock.pika.service.user;

import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final PasswordEncoder passwordEncoder;

    //ID,닉네임,이메일 중복 검사
    @Transactional(readOnly = true)
    public boolean checkUser(String id) {
        return userRepository.existsById(id);
    }
    @Transactional(readOnly = true)
    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);}
    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    //회원 가입
    @Transactional
    public Users joinUser(Users users) {
        //필수 요소 중복 확인
        if(checkUser(users.getId())) {
            throw new IllegalStateException("이미 존재하는 아이디 입니다.");
        }else if(checkNickname(users.getNickname())) {
            throw new IllegalStateException("이미 존재하는 닉네임 입니다");
        }else if(checkEmail(users.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일 입니다.");
        }
        //비밀번호 암호화
        users.setPw(passwordEncoder.encode(users.getPw()));
        return userRepository.save(users);
    }

    //회원 로그인
    @Transactional(readOnly = true)
    public Users login(String id, String password) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 아이디입니다."));

        //암호화된 비밀번호 일치 확인
        if (!passwordEncoder.matches(password, user.getPw())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    //회원 삭제
    @Transactional
    public void deleteUser(String userId, String rawPassword) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자 입니다."));
        //비밀번호 확인
        if(!passwordEncoder.matches(rawPassword, user.getPw())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
    }
}
