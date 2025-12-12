package com.numlock.pika.controller.main;

import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class Maincontroller {

    private final UserRepository userRepository;

    @GetMapping("/main")
    public String main(Principal principal, Model model){
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
        return "main";
    }
}
