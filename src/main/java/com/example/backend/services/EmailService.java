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

    public void sendAlert(Integer amount, Integer currentTotalAmount, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Budget Alert \uD83D\uDEA8");
        
        int exceededAmount = 0;
        if (currentTotalAmount != null && amount != null) {
            exceededAmount = currentTotalAmount - amount;
        }

        message.setText(
            "Hi,\n\n" +
            "This is a budget alert notification!\n" +
            "Budget Limit: ₹" + amount + "\n" +
            "Current Total Amount: ₹" + currentTotalAmount + "\n" +
            "Exceeded Amount: ₹" + exceededAmount + "\n\n" +
            "— Fin Tracker App"
        );

        mailSender.send(message);
    }
}
