package com.example.backend.dtos;

import lombok.Data;

@Data
public class AlertRequest {
    private Integer amount;
    private Integer currentTotalAmount;
    private String email;
}
