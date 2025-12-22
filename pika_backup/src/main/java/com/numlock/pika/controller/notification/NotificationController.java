package com.numlock.pika.controller.notification;

import com.numlock.pika.dto.NotificationDto;
import com.numlock.pika.service.Notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(Principal principal,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "2") int size) {
        if (principal != null) {
            String username = principal.getName();
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDto> notifications = notificationService.getUnreadNotifications(username, pageable);
            return ResponseEntity.ok(notifications);
        }
        return ResponseEntity.ok(Page.empty());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadNotificationCount(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            long count = notificationService.getUnreadNotificationCount(username);
            return ResponseEntity.ok(count);
        }
        return ResponseEntity.ok(0L);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
        System.out.println("읽음 업데이트 시작");
        if (principal != null) {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }
}

