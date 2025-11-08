// src/test/java/com/chatbot/ChatbotApplicationTests.java
package com.chatbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatbotApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("🚀 ChatBot Application Test - Context Loaded Successfully!");
    }

    @Test
    void applicationStartsTest() {
        ChatbotApplication.main(new String[] {});
        System.out.println("✅ Application started successfully in test!");
    }
}