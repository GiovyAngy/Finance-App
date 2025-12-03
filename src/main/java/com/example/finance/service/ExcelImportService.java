package com.example.finance.service;

import com.example.finance.model.Transaction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service("excelImportService")
public class ExcelImportService implements ImportService {

    @Override
    public List<Transaction> importData(InputStream inputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (Workbook wb = new XSSFWorkbook(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Transaction t = extractTransactionFromRow(row);
                if (t != null) {
                    transactions.add(t);
                }
            }
        }

        return transactions;
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("xlsx", "xls");
    }

    private Transaction extractTransactionFromRow(Row row) {
        try {
            Transaction t = new Transaction();

            if (row.getCell(1) != null) {
                t.setDate(row.getCell(1).getStringCellValue());
            }
            if (row.getCell(2) != null) {
                t.setType(row.getCell(2).getStringCellValue());
            }
            if (row.getCell(3) != null) {
                t.setCategory(row.getCell(3).getStringCellValue());
            }
            if (row.getCell(4) != null) {
                t.setDescription(row.getCell(4).getStringCellValue());
            }
            if (row.getCell(5) != null) {
                t.setAmount(row.getCell(5).getNumericCellValue());
            }

            return t;
        } catch (Exception e) {
            System.err.println("Fehler beim Importieren der Zeile: " + e.getMessage());
            return null;
        }
    }
}
