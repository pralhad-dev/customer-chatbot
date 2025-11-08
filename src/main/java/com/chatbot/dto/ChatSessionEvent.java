package com.chatbot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionEvent {
    private String sessionId;
    private String userId;
    private String userName;
    private String status; // ACTIVE, COMPLETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}