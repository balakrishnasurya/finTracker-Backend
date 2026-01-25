package com.example.backend.services;

import com.example.backend.dtos.ChatMessageDto;
import com.example.backend.dtos.ConversationDto;
import com.example.backend.dtos.CreateConversationDto;
import com.example.backend.dtos.SendMessageDto;
import com.example.backend.entities.ChatMessage;
import com.example.backend.entities.Conversation;
import com.example.backend.exceptions.AppException;
import com.example.backend.mappers.ChatMessageMapper;
import com.example.backend.mappers.ConversationMapper;
import com.example.backend.repositories.ChatMessageRepository;
import com.example.backend.repositories.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final CloudflareAiService cloudflareAiService;

    public ConversationDto createConversation(CreateConversationDto createConversationDto) {
        Conversation conversation = conversationMapper.toConversation(createConversationDto);

        Conversation savedConversation = conversationRepository.save(conversation);

        return conversationMapper.toConversationDto(savedConversation);
    }

    public List<ChatMessageDto> sendMessage(Long conversationId, SendMessageDto sendMessageDto) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("Conversation not found", HttpStatus.NOT_FOUND));

        // Save user message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversation(conversation);
        userMessage.setRole("user");
        userMessage.setContent(sendMessageDto.getMessage());
        chatMessageRepository.save(userMessage);

        // Generate AI response using Cloudflare AI
        String assistantResponse = cloudflareAiService.generateResponse(sendMessageDto.getMessage());

        // Save assistant message
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(assistantResponse);
        chatMessageRepository.save(assistantMessage);

        // Return all messages for this conversation
        return getConversationMessages(conversationId);
    }

    public List<ChatMessageDto> getConversationMessages(Long conversationId) {
        // Verify conversation exists
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException("Conversation not found", HttpStatus.NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return chatMessageMapper.toChatMessageDtos(messages);
    }
}
