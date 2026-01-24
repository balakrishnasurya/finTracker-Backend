package com.example.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionDto {
    private Long id;
    private Long smsId;
    private LocalDate txnDate;
    private BigDecimal amount;
    private String merchant;
    private String transactionType;
    private Long categoryId;
    private String categoryName;
    private String notes;
}
