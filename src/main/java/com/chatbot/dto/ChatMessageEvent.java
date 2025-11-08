package com.chatbot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageEvent {
    private String sessionId;
    private String message;
    private String senderType; // USER or BOT
    private String userId;
    private String userName;
    private LocalDateTime timestamp;
    private String messageType;
}