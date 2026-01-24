package com.example.backend.mappers;

import com.example.backend.dtos.ConversationDto;
import com.example.backend.dtos.CreateConversationDto;
import com.example.backend.entities.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationDto toConversationDto(Conversation conversation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Conversation toConversation(CreateConversationDto dto);
}
