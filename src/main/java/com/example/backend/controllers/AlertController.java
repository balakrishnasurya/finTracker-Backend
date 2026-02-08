package com.example.backend.controllers;

import com.example.backend.dtos.AlertRequest;
import com.example.backend.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/send-alert")
public class AlertController {

    private final EmailService emailService;

    public AlertController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<String> sendAlert(@RequestBody AlertRequest request) {
        emailService.sendAlert(
            request.getAmount(),
            request.getEmail()
        );

        return ResponseEntity.ok("Email sent successfully");
    }
}
