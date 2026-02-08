package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDetailResponse {
    private Long groupId;
    private String name;
    private String description;
    private List<MemberDto> members;
}
