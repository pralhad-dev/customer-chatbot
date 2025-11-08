package com.chatbot.service;

import com.chatbot.dto.*;
import com.chatbot.entity.ChatMessage;
import com.chatbot.entity.ChatSession;
import com.chatbot.repository.ChatMessageRepository;
import com.chatbot.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final KafkaProducerService kafkaProducerService;

    // Keyword patterns for intent recognition
    private static final Map<String, Pattern> KEYWORD_PATTERNS = new HashMap<>();

    static {
        KEYWORD_PATTERNS.put("greeting", Pattern.compile("(?i)(hello|hi|hey|good morning|good afternoon)"));
        KEYWORD_PATTERNS.put("pricing", Pattern.compile("(?i)(price|cost|how much|fee|charges)"));
        KEYWORD_PATTERNS.put("support", Pattern.compile("(?i)(help|support|issue|problem|error)"));
        KEYWORD_PATTERNS.put("product", Pattern.compile("(?i)(product|feature|service|what can you do)"));
        KEYWORD_PATTERNS.put("contact", Pattern.compile("(?i)(contact|email|phone|call|speak to human)"));
        KEYWORD_PATTERNS.put("thanks", Pattern.compile("(?i)(thank|thanks|appreciate)"));
        KEYWORD_PATTERNS.put("bye", Pattern.compile("(?i)(bye|goodbye|see you|later)"));
    }

    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        try {
            log.info("Processing message for session: {}", request.getSessionId());

            // Get or create session
            ChatSession session = getOrCreateSession(request);

            // Save user message
            saveUserMessage(session.getSessionId(), request.getMessage());

            // 🔥 KAFKA: Send user message to Kafka
            kafkaProducerService.sendChatMessage(
                    session.getSessionId(),
                    request.getMessage(),
                    "USER",
                    request.getUserId(),
                    request.getUserName(),
                    "TEXT"
            );

            // Process and generate bot response
            ChatResponse response = generateBotResponse(session, request.getMessage());

            // Save bot response
            saveBotMessage(session.getSessionId(), response.getBotResponse());

            // 🔥 KAFKA: Send bot response to Kafka
            kafkaProducerService.sendChatMessage(
                    session.getSessionId(),
                    response.getBotResponse(),
                    "BOT",
                    request.getUserId(),
                    request.getUserName(),
                    "TEXT"
            );

            // 🔥 KAFKA: Send analytics event
            String intent = detectIntent(request.getMessage());
            kafkaProducerService.sendAnalyticsEvent(
                    session.getSessionId(),
                    "MESSAGE_PROCESSED",
                    "TEXT",
                    request.getUserId(),
                    intent,
                    getMessageCount(session.getSessionId())
            );

            log.info("Message processed successfully for session: {}", request.getSessionId());
            return response;

        } catch (Exception e) {
            log.error("Error processing message for session: {}", request.getSessionId(), e);
            throw new RuntimeException("Failed to process message", e);
        }
    }

    private ChatSession getOrCreateSession(ChatRequest request) {
        return chatSessionRepository.findBySessionId(request.getSessionId())
                .orElseGet(() -> createNewSession(request));
    }

    private ChatSession createNewSession(ChatRequest request) {
        ChatSession newSession = ChatSession.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .userName(request.getUserName())
                .status(ChatSession.ChatStatus.ACTIVE)
                .build();

        ChatSession savedSession = chatSessionRepository.save(newSession);

        // Send welcome message for new session
        saveBotMessage(savedSession.getSessionId(),
                "Hello! I'm your customer support assistant. How can I help you today?");

        // 🔥 KAFKA: Send session created event
        kafkaProducerService.sendSessionEvent(
                savedSession.getSessionId(),
                request.getUserId(),
                request.getUserName(),
                "ACTIVE"
        );

        // 🔥 KAFKA: Send analytics event
        kafkaProducerService.sendAnalyticsEvent(
                savedSession.getSessionId(),
                "SESSION_STARTED",
                "SYSTEM",
                request.getUserId(),
                "new_session",
                0
        );

        log.info("Created new chat session: {}", savedSession.getSessionId());
        return savedSession;
    }

    private void saveUserMessage(String sessionId, String message) {
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .content(message)
                .senderType(ChatMessage.SenderType.USER)
                .messageType(ChatMessage.MessageType.TEXT)
                .isRead(true)
                .build();

        chatMessageRepository.save(userMessage);
        log.debug("Saved user message for session: {}", sessionId);
    }

    private void saveBotMessage(String sessionId, String message) {
        ChatMessage botMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .content(message)
                .senderType(ChatMessage.SenderType.BOT)
                .messageType(ChatMessage.MessageType.TEXT)
                .isRead(false)
                .build();

        chatMessageRepository.save(botMessage);
        log.debug("Saved bot message for session: {}", sessionId);
    }

    private ChatResponse generateBotResponse(ChatSession session, String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getSessionId());
        response.setTimestamp(LocalDateTime.now());
        response.setStatus("ACTIVE");

        // Keyword-based response logic
        if (KEYWORD_PATTERNS.get("greeting").matcher(lowerMessage).find()) {
            response.setBotResponse("Hello! Welcome to our customer support. I'm here to help you with:\n• Product Information\n• Pricing Details\n• Technical Support\n• Account Issues\n\nWhat would you like to know?");
            response.setQuickReplies(getMainQuickReplies());
        }
        else if (KEYWORD_PATTERNS.get("pricing").matcher(lowerMessage).find()) {
            response.setBotResponse(getPricingResponse());
            response.setQuickReplies(getPricingQuickReplies());
        }
        else if (KEYWORD_PATTERNS.get("support").matcher(lowerMessage).find()) {
            response.setBotResponse(getSupportResponse());
            response.setQuickReplies(getSupportQuickReplies());
        }
        else if (KEYWORD_PATTERNS.get("contact").matcher(lowerMessage).find()) {
            response.setBotResponse(getContactResponse());
            response.setQuickReplies(getContactQuickReplies());
        }
        else if (KEYWORD_PATTERNS.get("thanks").matcher(lowerMessage).find()) {
            response.setBotResponse("You're welcome! 😊 I'm glad I could help. Is there anything else you'd like to know?");
            response.setQuickReplies(getMainQuickReplies());
        }
        else if (KEYWORD_PATTERNS.get("bye").matcher(lowerMessage).find()) {
            response.setBotResponse("Thank you for chatting with us! Have a great day! 👋");
            session.setStatus(ChatSession.ChatStatus.COMPLETED);
            chatSessionRepository.save(session);
            response.setStatus("COMPLETED");

            // 🔥 KAFKA: Send session ended event
            kafkaProducerService.sendSessionEvent(
                    session.getSessionId(),
                    session.getUserId(),
                    session.getUserName(),
                    "COMPLETED"
            );
        }
        else {
            response.setBotResponse(getDefaultResponse(userMessage));
            response.setQuickReplies(getMainQuickReplies());
        }

        return response;
    }

    private String getPricingResponse() {
        return "Here's our pricing information:\n\n" +
                "💰 *Basic Plan:* $9.99/month\n" +
                "   - 10GB Storage\n" +
                "   - Basic Support\n" +
                "   - 5 Users\n\n" +
                "🚀 *Pro Plan:* $19.99/month\n" +
                "   - 50GB Storage\n" +
                "   - Priority Support\n" +
                "   - 25 Users\n\n" +
                "🏢 *Enterprise Plan:* $49.99/month\n" +
                "   - Unlimited Storage\n" +
                "   - 24/7 Support\n" +
                "   - Unlimited Users\n\n" +
                "Would you like more details about any specific plan?";
    }

    private String getSupportResponse() {
        return "I'm sorry you're experiencing issues. Let me help you!\n\n" +
                "🔧 *Common solutions:*\n" +
                "• Check your internet connection\n" +
                "• Clear browser cache\n" +
                "• Restart the application\n\n" +
                "If these don't work, please describe your issue in detail or contact our support team.";
    }

    private String getContactResponse() {
        return "You can contact our support team through:\n\n" +
                "📞 *Phone:* 1-800-123-4567\n" +
                "📧 *Email:* support@company.com\n" +
                "💬 *Live Chat:* Available 24/7\n" +
                "🕒 *Hours:* Mon-Sun, 9AM-9PM EST\n\n" +
                "Would you like me to connect you with a human agent?";
    }

    private String getDefaultResponse(String userMessage) {
        return "I understand you're asking about: \"" + userMessage + "\"\n\n" +
                "I can help you with:\n" +
                "• Product and pricing information\n" +
                "• Technical support\n" +
                "• Account issues\n" +
                "• Contact details\n\n" +
                "Please choose from the options below or ask me more specifically.";
    }

    // Quick Reply Methods
    private List<QuickReply> getMainQuickReplies() {
        return Arrays.asList(
                createQuickReply("💰 Pricing", "pricing"),
                createQuickReply("🛠️ Support", "support"),
                createQuickReply("📞 Contact", "contact"),
                createQuickReply("ℹ️ Features", "features")
        );
    }

    private List<QuickReply> getPricingQuickReplies() {
        return Arrays.asList(
                createQuickReply("Basic Plan", "basic plan"),
                createQuickReply("Pro Plan", "pro plan"),
                createQuickReply("Enterprise", "enterprise plan"),
                createQuickReply("Compare All", "compare plans")
        );
    }

    private List<QuickReply> getSupportQuickReplies() {
        return Arrays.asList(
                createQuickReply("Technical Issue", "technical help"),
                createQuickReply("Account Help", "account issue"),
                createQuickReply("Billing Problem", "billing issue"),
                createQuickReply("Human Agent", "speak to human")
        );
    }

    private List<QuickReply> getContactQuickReplies() {
        return Arrays.asList(
                createQuickReply("Call Support", "call support"),
                createQuickReply("Send Email", "send email"),
                createQuickReply("Live Chat", "live chat"),
                createQuickReply("Main Menu", "main menu")
        );
    }

    private QuickReply createQuickReply(String title, String payload) {
        QuickReply qr = new QuickReply();
        qr.setTitle(title);
        qr.setPayload(payload);
        return qr;
    }

    @Transactional(readOnly = true)
    public ChatHistoryResponse getChatHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        Optional<ChatSession> session = chatSessionRepository.findBySessionId(sessionId);

        ChatHistoryResponse history = new ChatHistoryResponse();
        history.setSessionId(sessionId);
        history.setSessionStart(session.map(ChatSession::getCreatedAt).orElse(LocalDateTime.now()));

        List<ChatMessageDTO> messageDTOs = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setContent(message.getContent());
            dto.setSenderType(message.getSenderType().name());
            dto.setMessageType(message.getMessageType().name());
            dto.setTimestamp(message.getTimestamp());
            dto.setIsRead(message.getIsRead());
            messageDTOs.add(dto);
        }

        history.setMessages(messageDTOs);
        return history;
    }

    @Transactional
    public void markMessagesAsRead(String sessionId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadUserMessages(sessionId);
        for (ChatMessage message : unreadMessages) {
            message.setIsRead(true);
        }
        chatMessageRepository.saveAll(unreadMessages);
        log.info("Marked {} messages as read for session: {}", unreadMessages.size(), sessionId);
    }

    public List<ChatSession> getActiveSessions() {
        return chatSessionRepository.findByStatus(ChatSession.ChatStatus.ACTIVE);
    }

    // Helper methods for Kafka
    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();
        for (Map.Entry<String, Pattern> entry : KEYWORD_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(lowerMessage).find()) {
                return entry.getKey();
            }
        }
        return "unknown";
    }

    private int getMessageCount(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId).size();
    }
}