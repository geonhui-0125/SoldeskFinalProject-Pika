package com.numlock.pika.controller.account;

import com.numlock.pika.service.account.AccountApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class AccountApiController {

    private final AccountApiService accountApiService;

    @GetMapping("/api/accounts/exists")
    public boolean existAccount(Principal principal) {
        return accountApiService.existAccount(principal);
    }

}
