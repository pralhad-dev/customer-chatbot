package com.chatbot.controller;

import com.chatbot.dto.ChatHistoryResponse;
import com.chatbot.dto.ChatRequest;
import com.chatbot.dto.ChatResponse;
import com.chatbot.entity.ChatSession;
import com.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatBotService chatBotService;

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        log.info("Received chat message for session: {}, user: {}",
                request.getSessionId(), request.getUserName());

        ChatResponse response = chatBotService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ChatHistoryResponse> getChatHistory(@PathVariable String sessionId) {
        log.info("Fetching chat history for session: {}", sessionId);
        ChatHistoryResponse history = chatBotService.getChatHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{sessionId}/mark-read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable String sessionId) {
        log.info("Marking messages as read for session: {}", sessionId);
        chatBotService.markMessagesAsRead(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<ChatSession>> getActiveSessions() {
        log.info("Fetching active chat sessions");
        List<ChatSession> activeSessions = chatBotService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok("ChatBot Service is running smoothly! " +
                "Time: " + java.time.LocalDateTime.now());
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Customer ChatBot API is working! " +
                "Use POST /api/v1/chat/send to send messages.");
    }
}