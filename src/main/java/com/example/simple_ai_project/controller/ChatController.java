package com.example.simple_ai_project.controller;

import com.example.simple_ai_project.dto.ChatHistoryResponse;
import com.example.simple_ai_project.dto.ResponseHandler;
import com.example.simple_ai_project.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final AiService aiService;

    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Object result = aiService.processFile(file);
            return ResponseHandler.success(result, "File processed successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/user-chats")
    public ResponseEntity<Object> getChats() {
        try {
            List<ChatHistoryResponse> chats = aiService.getUserChats();
            return ResponseHandler.success(chats, "User chats retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/history/{chatId}")
    public ResponseEntity<Object> deleteChatHistory(@PathVariable Long chatId) {
        try {
            String result = aiService.deleteChatHistory(chatId);
            return ResponseHandler.success(null, result, HttpStatus.OK);
        } catch (SecurityException e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
