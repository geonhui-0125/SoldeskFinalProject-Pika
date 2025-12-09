package com.numlock.pika.service.login;

import com.numlock.pika.dto.Users;
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
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users joinUser(Users users) {
        //ID 중복 확인
        if(checkUser(users.getId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        //비밀번호 암호화
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        return userRepository.save(users);
    }

    //ID 중복 검사
    public boolean checkUser(String id) {
        return userRepository.existsById(id);
    }


    @Transactional(readOnly = true)
    public Users login(String id, String password) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 아이디입니다."));

        //암호화된 비밀번호 일치 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Transactional
    public void deleteUser(String userId, String rawPassword) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자 입니다."));

        //비밀번호 확인
        if(!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }


}
