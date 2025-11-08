package com.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
        System.out.println("🚀 Customer ChatBot Application Started Successfully!");
        System.out.println("📞 API Base URL: http://localhost:8080/chatbot");
        System.out.println("💬 Health Check: http://localhost:8080/chatbot/api/v1/chat/health");
        System.out.println("🔄 Test API: http://localhost:8080/chatbot/api/v1/chat/test");
    }
}