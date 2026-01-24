package com.example.backend.mappers;

import com.example.backend.dtos.ChatMessageDto;
import com.example.backend.entities.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessageDto toChatMessageDto(ChatMessage chatMessage);

    List<ChatMessageDto> toChatMessageDtos(List<ChatMessage> chatMessages);
}
