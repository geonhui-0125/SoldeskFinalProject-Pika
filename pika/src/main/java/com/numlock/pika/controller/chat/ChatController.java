package com.numlock.pika.controller.chat;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.numlock.pika.domain.Message;
import com.numlock.pika.domain.Users;
import com.numlock.pika.dto.MessageDto;
import com.numlock.pika.service.CategoryService;
import com.numlock.pika.service.MessageService;
import com.numlock.pika.dto.ChatRoomDto;
import com.numlock.pika.service.chat.ChatRoomService;
import com.numlock.pika.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final UserService userService;
    private final MessageService messageService;
    private final ChatRoomService chatRoomService;
    private final CategoryService categoryService;


    @GetMapping("/list")
    public String chatList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        String userId = principal.getName();
        List<ChatRoomDto> chatRooms = chatRoomService.getChatRoomList(userId);
        model.addAttribute("chatRooms", chatRooms);
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);
        return "chat/chatList";
    }

    @GetMapping("/dm")
    public String dm(Model model, @RequestParam String recipientId,
                     @RequestParam(required = false) Integer productId, @AuthenticationPrincipal User currentUser) {
        String senderId = currentUser.getUsername();
        model.addAttribute("recipientId", recipientId);
        model.addAttribute("senderId", senderId);
        model.addAttribute("productId", productId);

        List<Message> conversation = messageService.getConversation(senderId, recipientId, productId);
        List<MessageDto> messageDtos = conversation.stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());

        System.out.println("**************debug code******************");
        Iterator<MessageDto> it = messageDtos.iterator();
        while(it.hasNext()) {
        	System.out.println(it.next().toString());
        }
        System.out.println("**************debug code******************");
        model.addAttribute("messages", messageDtos);
        Map<String, List<String>> categoriesMap = categoryService.getAllCategoriestoMap();
        model.addAttribute("categoriesMap", categoriesMap);
        return "chat/dm";
    }
}
