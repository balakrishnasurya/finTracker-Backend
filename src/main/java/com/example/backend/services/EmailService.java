package com.example.backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(Integer amount, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Budget Alert 🚨");
        message.setText(
            "Hi,\n\n" +
            "This is a budget alert notification.\n" +
            "Amount: ₹" + amount + "\n\n" +
            "— Fin Tracker App"
        );

        mailSender.send(message);
    }
}
