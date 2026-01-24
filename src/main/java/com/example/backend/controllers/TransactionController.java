package com.example.backend.controllers;

import com.example.backend.dtos.CreateTransactionDto;
import com.example.backend.dtos.TransactionDto;
import com.example.backend.dtos.UpdateTransactionDto;
import com.example.backend.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(from, to, categoryId));
    }

    @PostMapping
    @Operation(
            summary = "Create a new transaction",
            description = "Creates a new transaction. You can optionally link it to an SMS message by providing smsId (must be > 0), " +
                    "and assign it to a category by providing categoryId (must be > 0). If smsId or categoryId is 0 or null, they will be ignored.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                            content = @Content(schema = @Schema(implementation = TransactionDto.class))),
                    @ApiResponse(responseCode = "404", description = "SMS message or category not found")
            }
    )
    public ResponseEntity<TransactionDto> createTransaction(
            @RequestBody CreateTransactionDto createTransactionDto
    ) {
        TransactionDto created = transactionService.createTransaction(createTransactionDto);

        return ResponseEntity
                .created(URI.create("/api/v1/transactions/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @RequestBody UpdateTransactionDto updateTransactionDto
    ) {
        return ResponseEntity.ok(
                transactionService.updateTransaction(id, updateTransactionDto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TransactionDto> deleteTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.deleteTransaction(id));
    }
}
