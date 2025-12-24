package com.numlock.pika.controller.user;

import com.numlock.pika.config.JwtUtil;
import com.numlock.pika.dto.user.PasswordResetDto;
import com.numlock.pika.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/password")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PasswordResetController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    //1. 아이디/이메일 입력 폼 보여주기
    @GetMapping("/find")
    public String showFindPasswordForm() {
        return "user/findPasswordForm";
    }

    //2. 아이디/이메일 확인 후 인증번호 입력창 활성화
    @PostMapping("/find")
    public String requestPasswordReset(@RequestParam String id,
                                       @Email(message = "올바른 형식의 이메일을 입력해주세요.") String email,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
       try{
           boolean success = userService.handlePasswordResetRequest(id, email, request);
           if(success) {
               redirectAttributes.addFlashAttribute("successMessage", "비밀번호 재설정 이메일이 발송되었습니다. \n이메일을 확인해주세요.");
           } else {
               redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 이메일이 일치하는 사용자가 없습니다. \n다시 확인해주세요..");
           }
       } catch (Exception e){
           log.error("비밀번호 재설정 이메일 발송 중 오류 발생: {}", e.getMessage());
           redirectAttributes.addFlashAttribute("errorMessage", "이메일 발송 중 오류가 발생했습니다. 다시 시도해주세요.");
       }
        return "redirect:/user/password/find";
    }

    //3. 이메일 링크를 통해 새 비밀번호 입력폼으로 이동
    @GetMapping("/reset-form")
    public String showResetPasswordForm(@RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
        if (!jwtUtil.validateToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다.");
            return "redirect:/user/password/find";
        }
        //DTO에 토큰을 담아 뷰로 전달
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken(token);
        model.addAttribute("passwordResetDto", passwordResetDto);

        return "user/resetPasswordForm";
    }

    //4. 새 비밀번호 최종 업데이트 메서드
    @PostMapping("/reset-form")
    public String processResetPassword(@Valid @ModelAttribute("passwordResetDto") PasswordResetDto resetDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {
        //DTO 유효성 검사
        if (bindingResult.hasErrors()) {
            model.addAttribute("passwordResetDto", resetDto);
            return "user/resetPasswordForm";
        }
        //새 비밀번호와 비밀번호 확인 일치 여부 검사
        if (!resetDto.getNewPw().equals(resetDto.getConfirmNewPw())) {
            model.addAttribute("passwordResetDto", resetDto);
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "user/resetPasswordForm";
        }
            //서비스 레이어에서 비밀번호 재설정 처리
        try {
                boolean result = userService.resetPassword(resetDto.getToken(), resetDto.getNewPw());
                if (result) {
                    redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
                    return "redirect:/user/login";
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다.");
                    return "redirect:/user/password/find";
                }
        } catch (IllegalArgumentException e) {
                log.warn("비밀번호 재설정 실패: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/user/password/find";
        } catch (Exception e) {
                log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 재설정 중 오류가 발생했습니다.");
                return "redirect:/user/password/find";
        }
    }
}



