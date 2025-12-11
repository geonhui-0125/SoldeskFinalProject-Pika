package com.numlock.pika.controller.login;

import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.login.LoginService;
import com.numlock.pika.service.file.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;

    //Principal 객채로 index에 사용자 정보 호출
    @GetMapping("/")
    public String index(Principal principal, Model model) {
        if(principal != null) {
            //로그인한 사용자 아이디 호출
            String userId =  principal.getName();

            //아이디를 이용해 DB에서 사용자 조회
            userRepository.findById(userId).ifPresent(user -> {
                //조회된 Users 객체를 "user"라는 이름으로 모델에 추가
                model.addAttribute("user", user);
            });
            //아이디만 전송하는 코드
            model.addAttribute("loginUserId", userId);
        }
        return "index";
    }

    @GetMapping("/user/join")
    public String joinForm(Model model) {
        model.addAttribute("user", new Users());
        return "user/joinForm";
    }

    @PostMapping("/user/joinUser")
    public String joinUser(@Valid @ModelAttribute("user") Users user, BindingResult bindingResult,
                           @RequestParam(value = "profileImageFile", required = false)
                           MultipartFile profileImageFile, Model model) {
        log.info("Attempting to join user: {}", user.toString());

        if(bindingResult.hasErrors()) { //유효성 검사 실패 처리 로직
            log.warn("Validation errors for user join: {}", bindingResult.getAllErrors());
            return "user/joinForm";
        }

        try {
            String profileImagePath = null;
            // 1. 프로필 이미지 비어있이 않으면 저장
            if(profileImageFile != null && !profileImageFile.isEmpty()) {
                profileImagePath = fileUploadService.store(profileImageFile);
            }

            // 2. 프로필 이미지 업로드가 없거나 저장 실패시 기본이미지 설정
            if (profileImagePath == null) {
                profileImagePath = "/profile/default-profile.jpg"; // 기본 이미지 경로
            }
            user.setProfileImage(profileImagePath);

            // 3. 사용자 정보 저장
            user.setRole("USER"); // 일반 회원가입 시 USER 역할 부여
            loginService.joinUser(user);
            log.info("User {} joined successfully.", user.getId());

        } catch (IOException e) {
            log.error("File upload failed for user {}: {}", user.getId(), e.toString());
            model.addAttribute("errorMessage", "프로필 이미지 업로드에 실패했습니다.");
            model.addAttribute("user", user);
            return "user/joinForm";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            log.error("Error joining user {}: {}", user.getId(), e.toString());
            model.addAttribute("user", user);
            return "user/joinForm";
        }
        return "redirect:/";
    }

    //Spring Security가 로그인/아웃을 자동으로 처리
    @GetMapping("/user/login")
    public String loginForm(@RequestParam(required = false)String error, Model model) {
       if(error != null){
            model.addAttribute("errorMessage", "아이디 혹은 비밀번호가 올바르지 않습니다.");
        }

        model.addAttribute("user", new Users());
        return "user/loginForm";
    }

    //로그인 성공 처리
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
