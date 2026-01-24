package com.example.backend.services;

import com.example.backend.dtos.CreateTransactionDto;
import com.example.backend.dtos.TransactionDto;
import com.example.backend.dtos.UpdateTransactionDto;
import com.example.backend.entities.Category;
import com.example.backend.entities.SmsMessage;
import com.example.backend.entities.Transaction;
import com.example.backend.exceptions.AppException;
import com.example.backend.mappers.TransactionMapper;
import com.example.backend.repositories.CategoryRepository;
import com.example.backend.repositories.SmsMessageRepository;
import com.example.backend.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionDto> getTransactions(
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable Long categoryId
    ) {
        List<Transaction> transactions = transactionRepository.findTransactions(from, to, categoryId);
        return transactionMapper.toTransactionDtos(transactions);
    }

    public TransactionDto createTransaction(CreateTransactionDto createTransactionDto) {
        Transaction transaction = transactionMapper.toTransaction(createTransactionDto);

        // Set SMS message if provided
        if (createTransactionDto.getSmsId() != null && createTransactionDto.getSmsId() > 0) {
            SmsMessage smsMessage = smsMessageRepository.findById(createTransactionDto.getSmsId())
                    .orElseThrow(() -> new AppException("SMS message not found", HttpStatus.NOT_FOUND));
            transaction.setSmsMessage(smsMessage);
        }

        // Set category if provided
        if (createTransactionDto.getCategoryId() != null && createTransactionDto.getCategoryId() > 0) {
            Category category = categoryRepository.findById(createTransactionDto.getCategoryId())
                    .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));
            transaction.setCategory(category);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionDto(savedTransaction);
    }

    public TransactionDto updateTransaction(Long id, UpdateTransactionDto updateTransactionDto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new AppException("Transaction not found", HttpStatus.NOT_FOUND));

        // Set category if provided
        if (updateTransactionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateTransactionDto.getCategoryId())
                    .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));
            transaction.setCategory(category);
        }

        // Update notes
        if (updateTransactionDto.getNotes() != null) {
            transaction.setNotes(updateTransactionDto.getNotes());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionDto(savedTransaction);
    }

    public TransactionDto deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new AppException("Transaction not found", HttpStatus.NOT_FOUND));

        TransactionDto transactionDto = transactionMapper.toTransactionDto(transaction);

        // Soft delete
        transaction.setIsDeleted(true);
        transactionRepository.save(transaction);

        return transactionDto;
    }
}
