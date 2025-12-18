package com.numlock.pika.config;

import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("currentUserRole")
    public String currentUserRole(Principal principal, Model model) {
        if (principal != null) {
            return userRepository.findById(principal.getName())
                    .map(Users::getRole)
                    .orElse(null);
        }
        return null;
    }
}
