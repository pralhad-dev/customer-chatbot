package com.chatbot.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatHistoryResponse {
    private String sessionId;
    private List<ChatMessageDTO> messages;
    private LocalDateTime sessionStart;
}

