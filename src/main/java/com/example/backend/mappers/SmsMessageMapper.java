package com.example.backend.mappers;

import com.example.backend.dtos.CreateSmsMessageDto;
import com.example.backend.dtos.SmsMessageDto;
import com.example.backend.entities.SmsMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SmsMessageMapper {

    SmsMessageDto toSmsMessageDto(SmsMessage smsMessage);

    List<SmsMessageDto> toSmsMessageDtos(List<SmsMessage> smsMessages);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SmsMessage toSmsMessage(CreateSmsMessageDto dto);
}
