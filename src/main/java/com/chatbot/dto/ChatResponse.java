package com.chatbot.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatResponse {
    private String sessionId;
    private String botResponse;
    private List<QuickReply> quickReplies;
    private List<ChatOption> options;
    private LocalDateTime timestamp;
    private String status;
}

@Data
class ChatOption {
    private String text;
    private String value;
}