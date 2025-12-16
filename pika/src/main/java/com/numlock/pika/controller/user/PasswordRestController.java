package com.numlock.pika.controller.user;

import com.numlock.pika.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordRestController {

    private final UserService userService;

    //1. 아이디/이메일 입력 폼 보여주기
    @GetMapping("/find")
    public String showFindPasswordForm(){
        return "user/findPasswordForm";
    }

    //2. 아이디/이메일 확인 후 인증번호 입력창 활성화
/*    @PostMapping("/send-code")
    public String sendVerificationCode(@RequestParam String id,
                                       @RequestParam String email, Model model){
        boolean userExists = userService.checkUserByIdAndEmail(id, email);

        if
    }*/


}
