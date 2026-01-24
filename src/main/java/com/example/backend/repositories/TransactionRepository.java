package com.example.backend.repositories;

import com.example.backend.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false " +
            "AND (:from IS NULL OR t.txnDate >= :from) " +
            "AND (:to IS NULL OR t.txnDate <= :to) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId)")
    List<Transaction> findTransactions(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("categoryId") Long categoryId
    );

    boolean existsByCategoryIdAndIsDeletedFalse(Long categoryId);
}
