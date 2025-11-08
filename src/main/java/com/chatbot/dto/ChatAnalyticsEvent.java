package com.chatbot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatAnalyticsEvent {
    private String sessionId;
    private String eventType; // MESSAGE_SENT, SESSION_STARTED, SESSION_ENDED
    private String messageType;
    private String userId;
    private LocalDateTime timestamp;
    private int messageCount;
    private String intent; // pricing, support, contact, etc.
}