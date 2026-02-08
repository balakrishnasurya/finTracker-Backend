package com.example.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupMemberDto {
    private Long userId;
    
    @NotBlank(message = "Member name is required")
    private String name;
}
