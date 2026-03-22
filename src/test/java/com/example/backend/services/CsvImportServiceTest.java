package com.example.backend.services;

import com.example.backend.entities.Category;
import com.example.backend.repositories.CategoryRepository;
import com.example.backend.repositories.TransactionRepository;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private CsvImportService csvImportService;

    @BeforeEach
    void setUp() {
        csvImportService = new CsvImportService(transactionRepository, categoryRepository);
    }

    @Test
    void shouldImportCsvFileWithoutBreakingExistingBehavior() {
        Category category = new Category();
        category.setName("Groceries");
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(category));

        String csv = "txnDate,amount,merchant,paymentType,transactionDirection,categoryName,notes\n"
                + "2025-10-15,145.50,Surya,EXPENSE,DEBIT,Groceries,Weekly grocery run\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.csv",
                "text/csv",
                csv.getBytes()
        );

        String result = csvImportService.importTransactions(file);

        assertTrue(result.startsWith("CSV Import completed."));
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void shouldImportXlsxFileFromFirstSheet() throws Exception {
        Category category = new Category();
        category.setName("Groceries");
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(category));

        byte[] excelBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("txnDate");
            headerRow.createCell(1).setCellValue("amount");
            headerRow.createCell(2).setCellValue("merchant");
            headerRow.createCell(3).setCellValue("paymentType");
            headerRow.createCell(4).setCellValue("transactionDirection");
            headerRow.createCell(5).setCellValue("categoryName");
            headerRow.createCell(6).setCellValue("notes");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(java.sql.Date.valueOf(LocalDate.of(2025, 10, 15)));

            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
            dataRow.getCell(0).setCellStyle(dateStyle);

            dataRow.createCell(1).setCellValue(145.50);
            dataRow.createCell(2).setCellValue("Surya");
            dataRow.createCell(3).setCellValue("EXPENSE");
            dataRow.createCell(4).setCellValue("DEBIT");
            dataRow.createCell(5).setCellValue("Groceries");
            dataRow.createCell(6).setCellValue("Weekly grocery run");

            workbook.write(outputStream);
            excelBytes = outputStream.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        String result = csvImportService.importTransactions(file);

        assertTrue(result.startsWith("Excel Import completed."));
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    void shouldRejectUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.txt",
                "text/plain",
                "abc".getBytes()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> csvImportService.importTransactions(file));

        assertTrue(exception.getMessage().contains("Unsupported file format"));
    }
}
