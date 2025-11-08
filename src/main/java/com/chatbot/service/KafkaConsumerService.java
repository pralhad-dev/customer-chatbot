package com.chatbot.service;

import com.chatbot.dto.ChatAnalyticsEvent;
import com.chatbot.dto.ChatMessageEvent;
import com.chatbot.dto.ChatSessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    // Chat messages consume karega
    @KafkaListener(topics = "chat-messages", groupId = "chatbot-group")
    public void consumeChatMessages(ChatMessageEvent event) {
        log.info("📥 Consumed Chat Message - Session: {}, Sender: {}, Message: {}",
                event.getSessionId(), event.getSenderType(),
                event.getMessage().substring(0, Math.min(50, event.getMessage().length())) + "...");

        // Yahan pe aur processing kar sakte ho:
        // - Real-time notifications
        // - Message analytics
        // - Live dashboard updates
    }

    // Session events consume karega
    @KafkaListener(topics = "chat-sessions", groupId = "chatbot-group")
    public void consumeSessionEvents(ChatSessionEvent event) {
        log.info("📥 Consumed Session Event - Session: {}, User: {}, Status: {}",
                event.getSessionId(), event.getUserName(), event.getStatus());

        // Yahan pe aur processing kar sakte ho:
        // - Session monitoring
        // - User behavior tracking
        // - Performance metrics
    }

    // Analytics events consume karega
    @KafkaListener(topics = "chat-analytics", groupId = "chatbot-group")
    public void consumeAnalyticsEvents(ChatAnalyticsEvent event) {
        log.info("📊 Consumed Analytics Event - Session: {}, Event: {}, Intent: {}, Message Count: {}",
                event.getSessionId(), event.getEventType(), event.getIntent(), event.getMessageCount());

        // Yahan pe aur processing kar sakte ho:
        // - Business intelligence
        // - Reporting
        // - Dashboard updates
    }

    // Error handler
    @KafkaListener(topics = "chat-messages", groupId = "chatbot-group", errorHandler = "kafkaErrorHandler")
    public void consumeChatMessagesWithErrorHandling(ChatMessageEvent event) {
        try {
            consumeChatMessages(event);
        } catch (Exception e) {
            log.error("❌ Error processing chat message: {}", e.getMessage());
            // Yahan pe error handling logic add kar sakte ho
        }
    }
}