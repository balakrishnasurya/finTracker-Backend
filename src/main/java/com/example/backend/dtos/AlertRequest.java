package com.example.backend.dtos;

import lombok.Data;

@Data
public class AlertRequest {
    private Integer amount;
    private String email;
}
