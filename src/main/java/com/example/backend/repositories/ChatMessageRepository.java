package com.example.backend.repositories;

import com.example.backend.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
