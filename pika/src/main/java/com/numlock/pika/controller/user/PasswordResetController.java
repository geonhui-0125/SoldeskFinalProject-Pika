package com.numlock.pika.controller.user;

import com.numlock.pika.dto.PasswordResetDto;
import com.numlock.pika.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final UserService userService;

    //1. 아이디/이메일 입력 폼 보여주기
    @GetMapping("/find")
    public String showFindPasswordForm(){
        return "user/findPasswordForm";
    }

    //2. 아이디/이메일 확인 후 인증번호 입력창 활성화
    @PostMapping("/send-code")
    public String sendVerificationCode(@RequestParam String id,
                                       @RequestParam String email, Model model) {
        boolean userExists = userService.checkUserByIdAndEmail(id, email);

        if (userExists) {
            model.addAttribute("id", id);
            model.addAttribute("email", email);
            model.addAttribute("isCodeSent", true);
            model.addAttribute("successMessage", "인증번호를 확인하세요.(123456 입력)");
        } else {
            model.addAttribute("errorMessage", "입력하신 사용자를 확인할 수 없습니다.");
        }
        return "user/findPasswordForm";
    }

    //3. 인증번호 확인 후 비밀번호 재설정 페이지로 이동
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String id, @RequestParam String email, @RequestParam String verificationCode,
                              RedirectAttributes  redirectAttributes, Model model) {
        if("123456".equals(verificationCode)){
            redirectAttributes.addAttribute("userId", id);
            return "redirect:/user/password/reset";
        } else {
            model.addAttribute("id", id);
            model.addAttribute("email", email);
            model.addAttribute("isCodeSent", true);
            model.addAttribute("errorMessage", "인증번호가 올바르지 않습니다.");
            return "user/findPasswordForm";
        }
    }

    //4. 새 비밀번호 입력 폼으로 이동
    @GetMapping("/reset")
    public String showPasswordResetForm(@RequestParam String userId,
            @ModelAttribute("passwordResetDto") PasswordResetDto passwordResetDto,
            Model model) {
        passwordResetDto.setUserId(userId);
        model.addAttribute("userId", userId);
        return "user/resetPasswordForm";
    }

    //5. 새 비밀번호 최종 업데이트
    @PostMapping("/reset")
    public String processResetPassword(@Valid PasswordResetDto resetDto, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {
        //Dto유효성 검사
        if(bindingResult.hasErrors()){
            redirectAttributes.addAttribute("userId", resetDto.getUserId());
            bindingResult.getAllErrors().forEach((error) -> {
                redirectAttributes.addFlashAttribute("errorMessage", error.getDefaultMessage());
            });
            return "redirect:/user/password/reset";
        }

        //비밀번호 일치 여부 검사
        if (resetDto.getNewPw() == null || !resetDto.getNewPw().equals(resetDto.getConfirmNewPw())) {
            redirectAttributes.addAttribute("userId", resetDto.getUserId());
            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/user/password/reset";
        }
        try{
            userService.passwordReset(resetDto.getUserId(), resetDto.getNewPw());
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인 해주세요.");
            return "redirect:/user/login";
        }catch (Exception e){
            log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage());
            redirectAttributes.addAttribute("userId", resetDto.getUserId());
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "redirect:/user/password/reset";
        }
    }
}
