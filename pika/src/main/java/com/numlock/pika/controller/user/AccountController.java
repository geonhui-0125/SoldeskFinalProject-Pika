package com.numlock.pika.controller.user;

import com.numlock.pika.domain.Accounts;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.user.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    // 계좌 정보 저장 또는 수정 처리
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute("accounts") Accounts accountRequest,
                              Principal principal) {

        // 1. 현재 로그인한 사용자 정보를 SecurityContext에서 가져옴
        String userId = principal.getName();

        // 2. 계좌 정보 저장 서비스 호출
        accountService.saveOrUpdateAccount(accountRequest, userId);

        // 3. 다시 마이페이지로 이동
        return "redirect:/user/mypage";
    }

}