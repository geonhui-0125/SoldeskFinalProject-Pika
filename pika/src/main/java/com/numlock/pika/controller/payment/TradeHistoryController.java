package com.numlock.pika.controller.payment;

import com.numlock.pika.dto.payment.MyTradeHistoryDTO;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.payment.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeHistoryController {

    private final UserRepository userRepository;
    private final TradeService tradeService;

    @GetMapping("/history")
    public String tradeHistory(Model model, Principal principal) {

        if(principal != null) {
            //로그인한 사용자 아이디 호출
            String userId =  principal.getName();

            System.out.println("login한 사용자 : " + userId);

            //아이디를 이용해 DB에서 사용자 조회
            userRepository.findById(userId).ifPresent(user -> {
                model.addAttribute("user", user);

                MyTradeHistoryDTO myTradeHistoryDTO = tradeService.getMyTradeHistory(user);

                model.addAttribute("myTradeHistoryDTO", myTradeHistoryDTO);
            });
            model.addAttribute("loginUserId", userId);
        }

        return "payment/tradeHistory";
    }

}
