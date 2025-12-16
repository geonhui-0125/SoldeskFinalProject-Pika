package com.numlock.pika.controller.chat;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.numlock.pika.domain.Message;
import com.numlock.pika.domain.Users;
import com.numlock.pika.service.MessageService;
import com.numlock.pika.service.login.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final UserService userService;
    private final MessageService messageService; // Inject MessageService

    @GetMapping("/userList")
    public String userList(Model model, Principal principal) {
    	Users currentUser = userService.findById(principal.getName());
        List<Users> allUsers = userService.findAll();
        List<Users> otherUsers = allUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .toList();
        model.addAttribute("users", otherUsers);
        return "chat/userList";
    }

    @GetMapping("/dm")
    public String dm(Model model, @RequestParam String recipientId, @AuthenticationPrincipal User currentUser) {
        String senderId = currentUser.getUsername();
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("senderId", senderId);

        List<Message> conversation = messageService.getConversation(senderId, recipientId);
        System.out.println("**************debug code******************");
        Iterator<Message> it = conversation.iterator();
        while(it.hasNext()) {
        	System.out.println(it.next().toString());
        }
        System.out.println("**************debug code******************");
        model.addAttribute("messages", conversation);

        return "chat/dm";
    }
}
