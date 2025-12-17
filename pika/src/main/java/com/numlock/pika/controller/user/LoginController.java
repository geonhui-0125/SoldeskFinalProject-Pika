package com.numlock.pika.controller.user;

import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.UserDto;
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

import java.security.Principal;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    //Principal 객채로 main에 사용자 정보 호출
    @GetMapping("/")
    public String loginUser(Principal principal, Model model) {
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();

        System.out.println("categoriesMap 확인: " + categoriesMap);
        //카테고리 리스트 model로 전달
        model.addAttribute("categoriesMap", categoriesMap);

        if(principal != null) {
            //로그인한 사용자 아이디 호출
            String userId =  principal.getName();

            System.out.println("login한 사용자 : " + userId);

            //아이디를 이용해 DB에서 사용자 조회
            userRepository.findById(userId).ifPresent(user -> {
                //조회된 Users 객체를 "user"라는 이름으로 모델에 추가
                model.addAttribute("user", user);
            });

            //아이디만 전송하는 코드
            model.addAttribute("loginUserId", userId);
        }
        return "main";
    }

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
            model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
            return "user/joinForm";
        }

        try {
            //DTO -> Entity변환
            Users user = new Users();
            user.setId(userDto.getId());
            user.setPw(userDto.getPw());
            user.setNickname(userDto.getNickname());
            user.setEmail(userDto.getEmail());

            String profileImagePath = null;
            // 1. 프로필 이미지 비어있이 않으면 저장
            if(profileImageFile != null && !profileImageFile.isEmpty()) {
                profileImagePath = fileUploadService.storeImg(profileImageFile);
            }
            // 2. 프로필 이미지 업로드가 없거나 저장 실패시 기본이미지 설정
            if (profileImagePath == null) {
                profileImagePath = "/profile/default-profile.jpg"; // 기본 이미지 경로
            }
            user.setProfileImage(profileImagePath);
            // 3. 사용자 정보 저장
            user.setRole("GUEST"); // 일반 회원가입 시 GUEST 역할 부여
            System.out.println("회원가입 완료 ID: " +  user.getId() + ", 닉네임: " + user.getNickname());
            loginService.joinUser(user);
            log.info("User {} joined successfully.", user.getId());

        } catch (IOException e) {
            log.error("--- IOException Occurred ---", e);
            log.error("File upload failed for user {}: {}", userDto.getId(), e.toString());
            model.addAttribute("errorMessage", "프로필 이미지 업로드에 실패했습니다.");
            model.addAttribute("user", userDto);
            return "user/joinForm";
        } catch (Exception e) {
            log.error("--- Exception Occurred ---", e);
            model.addAttribute("errorMessage", "입력된 정보가 올바르지 않습니다. " + e.getMessage()); // 오류 메시지 복원
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
