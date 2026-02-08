package com.example.backend.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    
    @NotBlank(message = "Group name is required")
    private String name;
    
    private String description;
    
    @NotEmpty(message = "At least one member is required")
    @Valid
    private List<GroupMemberDto> members;
}
