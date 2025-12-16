package com.numlock.pika.controller.user;

import com.numlock.pika.dto.AdditionalUserInfoDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.user.LoginService;
import com.numlock.pika.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LoginService loginService;

    //회원 추가정보 입력 처리(Update)
    @GetMapping("/user/add-info")
    public String addInfoForm(Principal principal, Model model) {
        if(principal != null){
            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
            });
        }
        model.addAttribute("userAddInfo", new AdditionalUserInfoDto());
        return "user/addInfoForm";
    }

    @PostMapping("/user/add-info")
    public String addInfo(AdditionalUserInfoDto dto, Principal principal, Model model,
                          @RequestParam(value="profileImageFile", required = false)
                          MultipartFile profileImageFile, RedirectAttributes redirectAttributes) {
        if(principal == null) {
            //미로그인 오류 처리
            return "redirect:/";
        }
        try {
            userService.updateAddlInfo(principal.getName(), dto, profileImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "수정완료되었습니다.");
        }catch (Exception e){
            log.error("추가 정보 업데이트 중 오류 발생: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userAddInfo", dto);
            return "user/addInfoForm";
        }
        return "redirect:/";
    }

    //회원 탈퇴 처리(Delete)
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
