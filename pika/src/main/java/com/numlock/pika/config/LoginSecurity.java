package com.numlock.pika.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class LoginSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 암호화 원할 시 복구
        // 개발용. 비밀번호 암호화 해제
        //return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // .authorizeHttpRequests(authorize //특정 URL 모두 허용
                //                 -> authorize.requestMatchers("/user/join", "/user/login", "/", "/css/**", "/js/**").permitAll()
                //                 .requestMatchers("/mypage/**").hasRole("USER") // '/mypage/**'는 USER 역할만 접근 가능
                //         .anyRequest().authenticated() //나머지는 인증 필요
                // )
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
                // 모든 경로에 대한 접근 허용 (개발용) -> 마이페이지 등 구현 후 수정 필요
                .formLogin(form -> form.loginPage("/user/login") //사용자 정의 로그인 페이지
                        .usernameParameter("id") //loginForm의 username을 id로
                        .loginProcessingUrl("/user/login-proc") //로그인 처리 페이지
                        .defaultSuccessUrl("/", true) //로그인 성공시 리다이렉트
                        .failureUrl("/user/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/user/logout") //로그아웃 처리 URL
                        .logoutSuccessUrl("/") //로그아웃 성공시 리다이렉트
                        .invalidateHttpSession(true) //세션 무효화
                );

            return  http.build();
    }
}
