package com.example.backend.services;

import com.example.backend.dtos.StreakDto;
import com.example.backend.entities.Streak;
import com.example.backend.mappers.StreakMapper;
import com.example.backend.repositories.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakService {

    private static final String DEFAULT_STREAK_NAME = "app_usage";
    
    private final StreakRepository streakRepository;
    private final StreakMapper streakMapper;

    public StreakDto getStreak() {
        Streak streak = streakRepository.findByName(DEFAULT_STREAK_NAME)
                .orElseGet(() -> {
                    Streak newStreak = new Streak();
                    newStreak.setName(DEFAULT_STREAK_NAME);
                    newStreak.setCurrentCount(0);
                    newStreak.setLongestCount(0);
                    return streakRepository.save(newStreak);
                });
        
        return streakMapper.toStreakDto(streak);
    }

    public StreakDto updateStreak() {
        Streak streak = streakRepository.findByName(DEFAULT_STREAK_NAME)
                .orElseGet(() -> {
                    Streak newStreak = new Streak();
                    newStreak.setName(DEFAULT_STREAK_NAME);
                    newStreak.setCurrentCount(0);
                    newStreak.setLongestCount(0);
                    return newStreak;
                });

        LocalDate today = LocalDate.now();
        LocalDate lastUpdated = streak.getLastUpdated();

        if (lastUpdated == null) {
            // First time update
            streak.setCurrentCount(1);
            streak.setLongestCount(1);
        } else if (lastUpdated.equals(today)) {
            // Already updated today - return current streak without changes
            return streakMapper.toStreakDto(streak);
        } else if (lastUpdated.equals(today.minusDays(1))) {
            // Consecutive day - increment streak
            streak.setCurrentCount(streak.getCurrentCount() + 1);
            if (streak.getCurrentCount() > streak.getLongestCount()) {
                streak.setLongestCount(streak.getCurrentCount());
            }
        } else {
            // Streak broken - reset to 1
            streak.setCurrentCount(1);
        }

        streak.setLastUpdated(today);
        Streak savedStreak = streakRepository.save(streak);

        return streakMapper.toStreakDto(savedStreak);
    }
}
