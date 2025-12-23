package com.numlock.pika.controller;

import com.numlock.pika.service.GeminiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final GeminiService geminiService;

    @Autowired
    public AiChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askChat(@RequestBody Map<String, String> request, HttpSession session) {
        String userMessage = request.get("message");
        // 세션 ID를 사용하여 사용자별 대화 기록을 유지합니다.
        String responseMessage = geminiService.getChatResponse(session.getId(), userMessage);

        Map<String, String> response = new HashMap<>();
        response.put("response", responseMessage);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze/{productId}")
    public ResponseEntity<Map<String, String>> analyzeProduct(@PathVariable int productId) {
        String analysisResult = geminiService.analyzeProductPrice(productId);

        Map<String, String> response = new HashMap<>();
        response.put("response", analysisResult);

        return ResponseEntity.ok(response);
    }
}
