package com.numlock.pika.controller.main;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class Maincontroller {

    @GetMapping("/main")
    public String main(){
        return "main";
    }
}
