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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        String originalFilename = file.getOriginalFilename();
        String normalizedFilename = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);

        if (normalizedFilename.endsWith(".csv")) {
            return importFromCsv(file);
        }

        if (normalizedFilename.endsWith(".xlsx") || normalizedFilename.endsWith(".xls")) {
            return importFromExcel(file);
        }

        throw new RuntimeException("Unsupported file format. Please upload a .csv, .xlsx, or .xls file.");
    }

    private String importFromCsv(MultipartFile file) {
        ImportStats stats = new ImportStats();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

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
                    stats.transactions.add(tx);
                    stats.successCount++;
                } catch (Exception e) {
                    stats.errorCount++;
                    // Optionally log error for specific row
                }
            }

            if (!stats.transactions.isEmpty()) {
                transactionRepository.saveAll(stats.transactions);
            }

            return formatResult("CSV", stats);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    private String importFromExcel(MultipartFile file) {
        ImportStats stats = new ImportStats();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                return "File is empty";
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return "File is empty";
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new RuntimeException("Excel header row is missing");
            }

            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headerRow, dataFormatter);

            int firstDataRowIndex = headerRow.getRowNum() + 1;
            for (int rowIndex = firstDataRowIndex; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowBlank(row, dataFormatter, evaluator)) {
                    continue;
                }

                try {
                    Transaction tx = mapExcelRow(row, headerIndexMap, dataFormatter, evaluator);
                    stats.transactions.add(tx);
                    stats.successCount++;
                } catch (Exception e) {
                    stats.errorCount++;
                }
            }

            if (!stats.transactions.isEmpty()) {
                transactionRepository.saveAll(stats.transactions);
            }

            return formatResult("Excel", stats);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    private Map<String, Integer> buildHeaderIndexMap(Row headerRow, DataFormatter dataFormatter) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = headerRow.getFirstCellNum(); i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                continue;
            }
            String headerName = dataFormatter.formatCellValue(cell);
            if (headerName == null) {
                continue;
            }
            String normalizedHeader = headerName.trim();
            if (!normalizedHeader.isEmpty()) {
                headerIndexMap.put(normalizedHeader, i);
            }
        }
        return headerIndexMap;
    }

    private Transaction mapExcelRow(Row row,
                                    Map<String, Integer> headerIndexMap,
                                    DataFormatter dataFormatter,
                                    FormulaEvaluator evaluator) {
        Transaction tx = new Transaction();

        tx.setTxnDate(parseTxnDate(row, headerIndexMap, dataFormatter, evaluator));
        tx.setAmount(parseAmount(row, headerIndexMap, dataFormatter, evaluator));

        if (headerIndexMap.containsKey("merchant")) {
            tx.setMerchant(getExcelField(row, headerIndexMap, "merchant", dataFormatter, evaluator));
        }

        String paymentType = getExcelField(row, headerIndexMap, "paymentType", dataFormatter, evaluator);
        if (paymentType == null) {
            paymentType = getExcelField(row, headerIndexMap, "transactionType", dataFormatter, evaluator);
        }
        tx.setPaymentType(paymentType);

        String directionRaw = getExcelField(row, headerIndexMap, "transactionDirection", dataFormatter, evaluator);
        TransactionDirection direction = TransactionTypeResolver.parseDirection(directionRaw);
        tx.setTransactionType(TransactionTypeResolver.resolveDirection(direction, paymentType));

        if (headerIndexMap.containsKey("categoryName")) {
            String catName = getExcelField(row, headerIndexMap, "categoryName", dataFormatter, evaluator);
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

        if (headerIndexMap.containsKey("notes")) {
            tx.setNotes(getExcelField(row, headerIndexMap, "notes", dataFormatter, evaluator));
        }

        tx.setIsDeleted(false);
        return tx;
    }

    private LocalDate parseTxnDate(Row row,
                                   Map<String, Integer> headerIndexMap,
                                   DataFormatter dataFormatter,
                                   FormulaEvaluator evaluator) {
        Integer cellIndex = headerIndexMap.get("txnDate");
        if (cellIndex == null) {
            throw new IllegalArgumentException("Missing required column: txnDate");
        }

        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new IllegalArgumentException("txnDate is required");
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }

        if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }
        }

        String value = dataFormatter.formatCellValue(cell, evaluator);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("txnDate is required");
        }
        return LocalDate.parse(value.trim());
    }

    private BigDecimal parseAmount(Row row,
                                   Map<String, Integer> headerIndexMap,
                                   DataFormatter dataFormatter,
                                   FormulaEvaluator evaluator) {
        Integer cellIndex = headerIndexMap.get("amount");
        if (cellIndex == null) {
            throw new IllegalArgumentException("Missing required column: amount");
        }

        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new IllegalArgumentException("amount is required");
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        if (cell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = evaluator.evaluate(cell);
            if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cellValue.getNumberValue());
            }
        }

        String value = dataFormatter.formatCellValue(cell, evaluator);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("amount is required");
        }
        return new BigDecimal(value.replace(",", "").trim());
    }

    private String getExcelField(Row row,
                                 Map<String, Integer> headerIndexMap,
                                 String header,
                                 DataFormatter dataFormatter,
                                 FormulaEvaluator evaluator) {
        Integer cellIndex = headerIndexMap.get(header);
        if (cellIndex == null) {
            return null;
        }

        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value = dataFormatter.formatCellValue(cell, evaluator);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private boolean isRowBlank(Row row, DataFormatter dataFormatter, FormulaEvaluator evaluator) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                continue;
            }
            String value = dataFormatter.formatCellValue(cell, evaluator);
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String formatResult(String fileType, ImportStats stats) {
        return String.format("%s Import completed. Successfully processed: %d rows, Failed/Skipped: %d rows.",
                fileType,
                stats.successCount,
                stats.errorCount);
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

    private static class ImportStats {
        private final List<Transaction> transactions = new ArrayList<>();
        private int successCount;
        private int errorCount;
    }
}
