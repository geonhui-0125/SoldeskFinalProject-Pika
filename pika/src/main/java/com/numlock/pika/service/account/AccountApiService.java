package com.numlock.pika.service.account;

import com.numlock.pika.domain.Users;
import com.numlock.pika.repository.AccountRepository;
import com.numlock.pika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@RequiredArgsConstructor
@Service
public class AccountApiService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public boolean existAccount(Principal principal){

        Users users = userRepository.findById(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return accountRepository.existsAccountsBySeller(users);
    }
}
