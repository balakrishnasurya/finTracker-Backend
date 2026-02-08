package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupListResponse {
    private Long groupId;
    private String name;
    private String description;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
