package com.example.backend.services;

import com.example.backend.dtos.CreateSmsMessageDto;
import com.example.backend.dtos.SmsMessageDto;
import com.example.backend.entities.SmsMessage;
import com.example.backend.mappers.SmsMessageMapper;
import com.example.backend.repositories.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmsMessageService {

    private final SmsMessageRepository smsMessageRepository;
    private final SmsMessageMapper smsMessageMapper;

    public List<SmsMessageDto> allSmsMessages() {
        return smsMessageMapper.toSmsMessageDtos(smsMessageRepository.findAll());
    }

    public SmsMessageDto createSmsMessage(CreateSmsMessageDto createSmsMessageDto) {
        SmsMessage smsMessage = smsMessageMapper.toSmsMessage(createSmsMessageDto);

        SmsMessage savedSmsMessage = smsMessageRepository.save(smsMessage);

        return smsMessageMapper.toSmsMessageDto(savedSmsMessage);
    }
}
