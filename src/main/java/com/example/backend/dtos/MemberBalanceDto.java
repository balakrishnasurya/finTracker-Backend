package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberBalanceDto {
    private Long memberId;
    private String memberName;
    private BigDecimal totalPaid;
    private BigDecimal totalShare;
    private BigDecimal balance;
}
