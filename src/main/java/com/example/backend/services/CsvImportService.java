package com.example.backend.services;

import com.example.backend.entities.Category;
import com.example.backend.entities.TransactionDirection;
import com.example.backend.entities.Transaction;
import com.example.backend.repositories.CategoryRepository;
import com.example.backend.repositories.TransactionRepository;
import com.example.backend.utils.TransactionTypeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public String importTransactions(MultipartFile file) {
        if (file.isEmpty()) {
            return "File is empty";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            List<Transaction> transactions = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;

            for (CSVRecord record : csvParser) {
                try {
                    Transaction tx = new Transaction();
                    
                    // Mandatory fields
                    tx.setTxnDate(LocalDate.parse(record.get("txnDate")));
                    tx.setAmount(new BigDecimal(record.get("amount")));
                    
                    // Optional fields handling
                    if (record.isMapped("merchant")) {
                        tx.setMerchant(record.get("merchant"));
                    }

                    String paymentType = getField(record, "paymentType");
                    if (paymentType == null) {
                        paymentType = getField(record, "transactionType");
                    }
                    tx.setPaymentType(paymentType);

                    String directionRaw = getField(record, "transactionDirection");
                    TransactionDirection direction = TransactionTypeResolver.parseDirection(directionRaw);
                    tx.setTransactionType(TransactionTypeResolver.resolveDirection(direction, paymentType));
                    
                    // Category handling
                    if (record.isMapped("categoryName")) {
                        String catName = record.get("categoryName");
                        if (catName != null && !catName.trim().isEmpty()) {
                            Category category = categoryRepository.findByNameIgnoreCase(catName.trim())
                                    .orElseGet(() -> {
                                        Category newCat = new Category();
                                        newCat.setName(catName.trim());
                                        return categoryRepository.save(newCat);
                                    });
                            tx.setCategory(category);
                        }
                    }

                    if (record.isMapped("notes")) {
                        tx.setNotes(record.get("notes"));
                    }
                    
                    tx.setIsDeleted(false);
                    transactions.add(tx);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    // Optionally log error for specific row
                }
            }

            if (!transactions.isEmpty()) {
                transactionRepository.saveAll(transactions);
            }

            return String.format("CSV Import completed. Successfully processed: %d rows, Failed/Skipped: %d rows.", successCount, errorCount);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    private String getField(CSVRecord record, String header) {
        if (!record.isMapped(header)) {
            return null;
        }
        String value = record.get(header);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
