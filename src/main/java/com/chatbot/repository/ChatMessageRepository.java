package com.chatbot.repository;

import com.chatbot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sessionId = :sessionId AND cm.senderType = 'USER' AND cm.isRead = false")
    List<ChatMessage> findUnreadUserMessages(@Param("sessionId") String sessionId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.sessionId = :sessionId AND cm.senderType = 'USER' AND cm.isRead = false")
    Long countUnreadUserMessages(@Param("sessionId") String sessionId);
}