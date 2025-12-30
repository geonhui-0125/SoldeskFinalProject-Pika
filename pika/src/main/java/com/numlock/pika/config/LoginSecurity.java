package com.numlock.pika.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 메서드 보안 활성화
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
                /*.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/icon/**", "/upload/**", "/profile/**").permitAll()
                        .requestMatchers("/", "/main", "/user/login", "user/login-proc", "/user/join", "/search/**", "user/password/**").permitAll()
                        //마이페이지, 상품등록 가입사 용자 모두 접근 가능(USER/GUEST 권환 분리 논의 필요)
                        .requestMatchers("/search/**", "products/search", "products/info/**", "/products/bySeller/**").permitAll()
                        .requestMatchers("/user/mypage/**", "/products/new", "/chat/**").authenticated()
                        .anyRequest().authenticated() //나머지는 인증 불필요
                )*/
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
                // 모든 경로에 대한 접근 허용 (개발용) 개발 완료후 삭제 필요
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
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // 인증된 사용자가 권한 없는 페이지 접근 시 메인 페이지로 리다이렉트
                        .accessDeniedPage("/")
                );

            return  http.build();
    }
}
