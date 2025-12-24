package com.numlock.pika.controller.user;

import com.numlock.pika.domain.Users;

import com.numlock.pika.dto.user.UserDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.service.user.LoginService;
import com.numlock.pika.service.file.FileUploadService;
import com.numlock.pika.service.user.UserService;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    //회원 가입 처리(Create)
    @GetMapping("/user/join")
    public String joinForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "user/joinForm";
    }

    @PostMapping("/user/joinUser")
    public String joinUser(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult,
                           @RequestParam(value = "profileImageFile", required = false)
                           MultipartFile profileImageFile, Model model) {
        log.info("Attempting to join user with DTO: {}", userDto.toString());

        if(bindingResult.hasErrors()) { //유효성 검사 실패 처리 로직/DTO유효성 검사
            log.error("--- Validation Failed ---");
            log.error("Validation errors for user join: {}", bindingResult.getAllErrors());
            //model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
            return "user/joinForm";
        }

        //아이디, 닉네임, 이메일 중복체크
        if(userRepository.existsById(userDto.getId())){
            bindingResult.rejectValue("id", "duplicated", "이미 사용중인 아이디 입니다.");
            return "user/joinForm";
        }
        if(userRepository.existsByNickname(userDto.getNickname())){
            bindingResult.rejectValue("nickname", "duplicated", "이미 사용중인 닉네임 입니다.");
            return "user/joinForm";
        }
        if(userRepository.existsByEmail(userDto.getEmail())){
            bindingResult.rejectValue("email", "duplicated", "이미 사용중인 이메일 입니다.");
            return "user/joinForm";
        }

        //비밀번호와 비밀번호 확인 일치 여부 검사
        if(!userDto.getPw().equals(userDto.getConfirmPw())){
            bindingResult.rejectValue("confirmPw", "passwordMismatch", "비밀번호가 일치하지 않습니다.");
            log.error("---Password Mismatch");
            log.error("패스워드와 패스워드 확인이 일치하지 않습니다. 유저ID: {}", userDto.getId());
            //model.addAttribute("errorMessage", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "user/joinForm";
        }



        try {
            //DTO -> Entity변환
            Users user = new Users();
            user.setId(userDto.getId());
            user.setPw(userDto.getPw());
            user.setNickname(userDto.getNickname());
            user.setEmail(userDto.getEmail());

            String profileImagePath = "/profile/default-profile.png";
            user.setProfileImage(profileImagePath);
            // 사용자 정보 저장
            user.setRole("GUEST"); // 일반 회원가입 시 GUEST 역할 부여
            System.out.println("회원가입 완료 ID: " +  user.getId() + ", 닉네임: " + user.getNickname());
            loginService.joinUser(user);
            log.info("User {} joined successfully.", user.getId());

        } catch (Exception e) {
            log.error("--- Exception Occurred ---", e);
            model.addAttribute("errorMessage", " " + e.getMessage()); // 오류 메시지 복원
            log.error("Error joining user {}: {}", userDto.getId(), e.toString());
            model.addAttribute("user", userDto);
            return "user/joinForm";
        }
        return "/user/loginForm";
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
}
