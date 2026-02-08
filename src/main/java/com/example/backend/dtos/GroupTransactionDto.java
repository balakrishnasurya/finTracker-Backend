package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupTransactionDto {
    private Long transactionId;
    private String description;
    private BigDecimal amount;
    private String paidByMemberName;
    private Long paidByMemberId;
    private List<String> participantNames;
    private LocalDateTime transactionDate;
}
