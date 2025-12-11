package com.numlock.pika.service.login;

import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {

    private final UserRepository userRepository;

    @Transactional
    public Users joinUser(Users users) {
        //ID 중복 확인
        if(checkUser(users.getId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }else if(checkNickname(users.getNickname())) {
            throw new IllegalStateException("이미 존재하는 닉네임 입니다");
        }
        //비밀번호 암호화
        // users.setPs(passwordEncoder.encode(users.getPs()));
        return userRepository.save(users);
    }

    //ID 중복 검사
    public boolean checkUser(String id) {
        return userRepository.existsById(id);
    }

    //닉네임 중복 검사
    public boolean checkNickname(String nickname) {return userRepository.existsByNickname(nickname);}

    @Transactional(readOnly = true)
    public Users login(String id, String password) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 아이디입니다."));

        //암호화된 비밀번호 일치 확인 (현재 단계에서는 평문으로 직접 비교)
        // if (!passwordEncoder.matches(password, user.getPs())) {
        //     throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        // }
        if (!password.equals(user.getPw())) { // 평문 비밀번호 직접 비교
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    @Transactional
    public void deleteUser(String userId, String rawPassword) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자 입니다."));

        //비밀번호 확인 (평문 비교)
        if(!rawPassword.equals(user.getPw())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
    }

}
