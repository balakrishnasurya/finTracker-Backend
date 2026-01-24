package com.example.backend.repositories;

import com.example.backend.entities.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {
    
    Optional<Streak> findByName(String name);
}
