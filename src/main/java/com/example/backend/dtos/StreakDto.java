package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StreakDto {
    private Long id;
    private String name;
    private Integer currentCount;
    private Integer longestCount;
    private LocalDate lastUpdated;
}
