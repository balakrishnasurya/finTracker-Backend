package com.example.backend.controllers;

import com.example.backend.dtos.CreateSmsMessageDto;
import com.example.backend.dtos.SmsMessageDto;
import com.example.backend.services.SmsMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sms")
public class SmsMessageController {

    private final SmsMessageService smsMessageService;

    @GetMapping
    public ResponseEntity<List<SmsMessageDto>> allSmsMessages() {
        return ResponseEntity.ok(smsMessageService.allSmsMessages());
    }

    @PostMapping
    public ResponseEntity<SmsMessageDto> createSmsMessage(
            @RequestBody CreateSmsMessageDto createSmsMessageDto
    ) {
        SmsMessageDto created = smsMessageService.createSmsMessage(createSmsMessageDto);

        return ResponseEntity
                .created(URI.create("/api/v1/sms/" + created.getId()))
                .body(created);
    }
}
