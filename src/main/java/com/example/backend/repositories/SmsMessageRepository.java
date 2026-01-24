package com.example.backend.repositories;

import com.example.backend.entities.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {
}
