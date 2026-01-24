package com.example.backend.mappers;

import com.example.backend.dtos.CreateTransactionDto;
import com.example.backend.dtos.TransactionDto;
import com.example.backend.dtos.UpdateTransactionDto;
import com.example.backend.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "smsMessage.id", target = "smsId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    TransactionDto toTransactionDto(Transaction transaction);

    List<TransactionDto> toTransactionDtos(List<Transaction> transactions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "smsMessage", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toTransaction(CreateTransactionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "smsMessage", ignore = true)
    @Mapping(target = "txnDate", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "transactionType", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTransaction(@MappingTarget Transaction transaction, UpdateTransactionDto dto);
}
