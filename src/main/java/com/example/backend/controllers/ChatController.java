package com.example.backend.controllers;

import com.example.backend.dtos.ChatMessageDto;
import com.example.backend.dtos.ConversationDto;
import com.example.backend.dtos.CreateConversationDto;
import com.example.backend.dtos.SendMessageDto;
import com.example.backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDto> createConversation(
            @RequestBody CreateConversationDto createConversationDto
    ) {
        ConversationDto created = chatService.createConversation(createConversationDto);

        return ResponseEntity
                .created(URI.create("/api/v1/chat/conversations/" + created.getId()))
                .body(created);
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatMessageDto>> sendMessage(
            @PathVariable Long id,
            @RequestBody SendMessageDto sendMessageDto
    ) {
        return ResponseEntity.ok(chatService.sendMessage(id, sendMessageDto));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<ChatMessageDto>> getConversationMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getConversationMessages(id));
    }
}
