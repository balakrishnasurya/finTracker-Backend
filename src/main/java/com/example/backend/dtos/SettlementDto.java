package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementDto {
    private String fromMember;
    private String toMember;
    private BigDecimal amount;
}
