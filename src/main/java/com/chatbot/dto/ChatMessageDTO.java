package com.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public
class ChatMessageDTO {
    private String content;
    private String senderType;
    private String messageType;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
