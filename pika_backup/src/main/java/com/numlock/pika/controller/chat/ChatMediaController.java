package com.numlock.pika.controller.chat;

import com.numlock.pika.service.file.ChatMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatMediaController {

    private final ChatMediaService chatMediaService;

    @PostMapping("/chat/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            String filePath = chatMediaService.store(file);
            return ResponseEntity.ok(Map.of("filePath", filePath));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not store the file. Error: " + e.getMessage()));
        }
    }
}
