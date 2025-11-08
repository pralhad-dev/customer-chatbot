# Customer Support ChatBot 🤖

A production-ready Spring Boot based customer support chatbot without AI, featuring interactive conversations, session management, and real-time messaging.

## 🚀 Features

- **💬 Interactive Chat Interface** - Quick replies and options
- **🔐 Session Management** - User sessions with persistence
- **💾 Database Storage** - MySQL with Spring Data JPA
- **🔄 REST APIs** - Clean and documented endpoints
- **📊 Chat History** - Complete conversation history
- **⚡ Real-time Ready** - Scalable architecture

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.x
- **Database:** MySQL 8.x
- **ORM:** Spring Data JPA
- **Build Tool:** Maven
- **API Documentation:** Spring REST Docs

## 📋 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/chat/send` | Send message to chatbot |
| GET | `/api/v1/chat/history/{sessionId}` | Get chat history |
| POST | `/api/v1/chat/{sessionId}/mark-read` | Mark messages as read |
| GET | `/api/v1/chat/health` | Health check |

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 17 or higher
- MySQL 8.x
- Maven 3.6+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/customer-chatbot.git
cd customer-chatbot
