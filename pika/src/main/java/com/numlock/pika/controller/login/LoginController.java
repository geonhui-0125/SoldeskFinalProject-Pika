package com.numlock.pika.controller.login;

import com.numlock.pika.domain.Users;
import com.numlock.pika.service.login.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    //Principal 객채로 사용자 정보 호출
    @GetMapping("/")
    public String index(Principal principal, Model model) {
        if(principal != null) {
            model.addAttribute("loginUserId", principal.getName());
        }
        return "index";
    }

    @GetMapping("/user/join")
    public String joinForm(Model model) {
        model.addAttribute("user", new Users()); // Changed from User to Users
        return "user/joinForm";
    }

    @PostMapping("/user/joinUser")
    public String joinUser(Users users, Model model) { // Changed from User to Users
        log.info("Attempting to join user: {}", users.toString()); // Log the whole object

        try {
            users.setRole("USER"); // 일반 회원가입 시 USER 역할 부여
            loginService.joinUser(users);
            log.info("User {} joined successfully.", users.getId());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            log.error("Error joining user {}: {}", users.getId(), e.toString());
            return "user/joinForm";
        }
        return "redirect:/";
    }

    //Spring Security가 로그인/아웃을 자동으로 처리
    @GetMapping("/user/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new Users());
        return "user/loginForm";
    }

    @GetMapping("/user/loginSuccess")
    public String loginSuccess(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("loginUserId", principal.getName());
        }
        return "user/loginSuccess";
    }

    @GetMapping("/user/delete")
    public String deleteForm() {
        return "/user/deleteForm";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam String rawPassword, Principal principal, HttpServletRequest request, Model model) {
        if(principal == null) {
            return "redirect:/user/login"; //로그인 하지 않은 사용자 접근시
        }

        try {
            loginService.deleteUser(principal.getName(), rawPassword);
            request.logout();
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "/user/deleteForm";
        }

    }

}
