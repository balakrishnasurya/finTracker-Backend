package com.example.backend.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateGroupTransactionRequest {
    
    private String description;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Paid by member ID is required")
    private Long paidByMemberId;
    
    @NotEmpty(message = "At least one participant is required")
    private List<Long> includedMemberIds;
}
