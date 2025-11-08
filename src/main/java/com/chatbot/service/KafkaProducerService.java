package com.chatbot.service;

import com.chatbot.dto.ChatAnalyticsEvent;
import com.chatbot.dto.ChatMessageEvent;
import com.chatbot.dto.ChatSessionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Chat messages ko Kafka topic pe send karega
    public void sendChatMessage(String sessionId, String message, String senderType,
                                String userId, String userName, String messageType) {
        try {
            ChatMessageEvent event = new ChatMessageEvent();
            event.setSessionId(sessionId);
            event.setMessage(message);
            event.setSenderType(senderType);
            event.setUserId(userId);
            event.setUserName(userName);
            event.setTimestamp(LocalDateTime.now());
            event.setMessageType(messageType);

            kafkaTemplate.send("chat-messages", sessionId, event);
            log.info("📤 Sent chat message to Kafka topic: session={}, sender={}", sessionId, senderType);

        } catch (Exception e) {
            log.error("❌ Failed to send chat message to Kafka: {}", e.getMessage());
        }
    }

    // Session events ko Kafka topic pe send karega
    public void sendSessionEvent(String sessionId, String userId, String userName, String status) {
        try {
            ChatSessionEvent event = new ChatSessionEvent();
            event.setSessionId(sessionId);
            event.setUserId(userId);
            event.setUserName(userName);
            event.setStatus(status);
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());

            kafkaTemplate.send("chat-sessions", sessionId, event);
            log.info("📤 Sent session event to Kafka: session={}, status={}", sessionId, status);

        } catch (Exception e) {
            log.error("❌ Failed to send session event to Kafka: {}", e.getMessage());
        }
    }

    // Analytics events ko Kafka topic pe send karega
    public void sendAnalyticsEvent(String sessionId, String eventType, String messageType,
                                   String userId, String intent, int messageCount) {
        try {
            ChatAnalyticsEvent event = new ChatAnalyticsEvent();
            event.setSessionId(sessionId);
            event.setEventType(eventType);
            event.setMessageType(messageType);
            event.setUserId(userId);
            event.setTimestamp(LocalDateTime.now());
            event.setIntent(intent);
            event.setMessageCount(messageCount);

            kafkaTemplate.send("chat-analytics", sessionId, event);
            log.info("📊 Sent analytics event to Kafka: session={}, event={}", sessionId, eventType);

        } catch (Exception e) {
            log.error("❌ Failed to send analytics event to Kafka: {}", e.getMessage());
        }
    }
}