package com.example.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GymRecordDto {
    private Long id;
    private String exercise;
    private Integer weight;
}

