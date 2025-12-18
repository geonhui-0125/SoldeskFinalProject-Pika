package com.numlock.pika.config;

import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // "admin" 사용자가 없는 경우에만 생성
        if (userRepository.findById("admin").isEmpty()) {
            Users adminUser = Users.builder()
                    .id("admin")
                    .pw(passwordEncoder.encode("ad123")) // 안전한 비밀번호로 변경 권장
                    .nickname("관리자")
                    .email("admin@example.com")
                    .profileImage("/profile/admin_hat.png") // 프로필 이미지 플레이스홀더 추가
                    .role("ADMIN")
                    .build();
            /*
            // adminUser.setWarningCount(0); // 세터로 warningCount 설정
            // adminUser.setIsRestricted(false); // 세터로 isRestricted 설정
            */
            userRepository.save(adminUser);
            System.out.println("Admin user 'admin' created successfully.");
        } else {
            System.out.println("Admin user 'admin' already exists.");
        }
    }
}
