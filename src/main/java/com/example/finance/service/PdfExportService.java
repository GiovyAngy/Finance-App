package com.example.finance.service;


import com.example.finance.model.Transaction;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("pdfExportService")
public class PdfExportService implements ExportService {

    @Override
    public void export(List<Transaction> transactions, OutputStream outputStream) throws IOException {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Haupttile
            Paragraph title = new Paragraph("Finanzbericht")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Zusammenfassung
            addSummarySection(document, transactions);

            // Alle Transaktionen
            addTransactionsTable(document, transactions);

            // Ausgaben nach Kategorie
            addExpensesbyCategory(document, transactions);

            addIncomebyCategory(document, transactions);

            document.close();
        } catch (Exception e) {
            throw new IOException("Fehler beim PDF-Export: " + e.getMessage(), e);
        }
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getDefaultFileName() {
        return "finance_export.pdf";
    }

    private void addSummarySection(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("Zusammenfassung")
                .setFontSize(16)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10));

        double totalIncome = transactions.stream()
                .filter(t -> "EINNAHMEN".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = transactions.stream()
                .filter(t -> "AUSGABEN".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double balance = totalIncome - Math.abs(totalExpense);

        Table summaryTable = new Table(2);
        summaryTable.setWidth(UnitValue.createPercentValue(50));

        addSummaryRow(summaryTable, "Gesamte Einnahmen:",
                String.format("%.2f €", totalIncome),
                new DeviceRgb(76, 175, 80));
        addSummaryRow(summaryTable, "Gesamte Ausgaben:",
                String.format("%.2f €", Math.abs(totalExpense)),
                new DeviceRgb(244, 67, 54));
        addSummaryRow(summaryTable, "Kontostand:",
                String.format("%.2f €", balance),
                balance >= 0 ? new DeviceRgb(33, 150, 243) : new DeviceRgb(244, 67, 54));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }

    private void addSummaryRow(Table table, String label, String value, DeviceRgb color) {
        table.addCell(new Cell().add(new Paragraph(label).setBold())
                .setBackgroundColor(new DeviceRgb(245, 245, 245)));
        table.addCell(new Cell().add(new Paragraph(value).setBold())
                .setFontColor(color));
    }

    private void addTransactionsTable(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("Alle Transaktionen")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));

        float[] columnWidths = {1, 2, 2, 2, 4, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Header
        String[] headers = {"ID", "Datum", "Typ", "Kategorie", "Beschreibung", "Betrag"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(new DeviceRgb(63, 81, 181))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Transaction
        for (Transaction t : transactions) {
            table.addCell(new Cell().add(new Paragraph(
                            t.getId() != null ? String.valueOf(t.getId()) : ""))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(
                    t.getDate() != null ? t.getDate() : "")));

            // Farbe Typ
            DeviceRgb typeColor = "EINNAHMEN".equals(t.getType())
                    ? new DeviceRgb(76, 175, 80)
                    : new DeviceRgb(244, 67, 54);
            table.addCell(new Cell().add(new Paragraph(t.getType()))
                    .setFontColor(typeColor)
                    .setBold());

            table.addCell(new Cell().add(new Paragraph(
                    t.getCategory() != null ? t.getCategory() : "")));
            table.addCell(new Cell().add(new Paragraph(
                    t.getDescription() != null ? t.getDescription() : "")));

            // Bertrag Format 00,00€
            String amountStr = String.format("%.2f €", t.getAmount());
            table.addCell(new Cell().add(new Paragraph(amountStr))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold());
        }

        document.add(table);
    }

    private void addExpensesbyCategory(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("\nAusgaben nach Kategorie")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));

        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> "AUSGABEN".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Keine Kategorie",
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        if (categoryTotals.isEmpty()) {
            document.add(new Paragraph("Keine Ausgaben vorhanden"));
            return;
        }

        Table categoryTable = new Table(2);
        categoryTable.setWidth(UnitValue.createPercentValue(60));

        categoryTable.addHeaderCell(new Cell()
                .add(new Paragraph("Kategorie").setBold())
                .setBackgroundColor(new DeviceRgb(63, 81, 181))
                .setFontColor(ColorConstants.WHITE));
        categoryTable.addHeaderCell(new Cell()
                .add(new Paragraph("Summe").setBold())
                .setBackgroundColor(new DeviceRgb(63, 81, 181))
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.RIGHT));

        categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    categoryTable.addCell(new Cell().add(new Paragraph(entry.getKey())));
                    categoryTable.addCell(new Cell()
                            .add(new Paragraph(String.format("%.2f €", entry.getValue())))
                            .setTextAlignment(TextAlignment.RIGHT));
                });

        document.add(categoryTable);
    }


    private void addIncomebyCategory(Document document, List<Transaction> transactions) {
        document.add(new Paragraph("\nEinnahmen nach Kategorie")
                .setFontSize(16)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10));

        Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> "EINNAHMEN".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Keine Kategorie",
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        if (categoryTotals.isEmpty()) {
            document.add(new Paragraph("Keine Ausgaben vorhanden"));
            return;
        }

        Table categoryTable = new Table(2);
        categoryTable.setWidth(UnitValue.createPercentValue(60));

        categoryTable.addHeaderCell(new Cell()
                .add(new Paragraph("Kategorie").setBold())
                .setBackgroundColor(new DeviceRgb(63, 81, 181))
                .setFontColor(ColorConstants.WHITE));
        categoryTable.addHeaderCell(new Cell()
                .add(new Paragraph("Summe").setBold())
                .setBackgroundColor(new DeviceRgb(63, 81, 181))
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.RIGHT));

        categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    categoryTable.addCell(new Cell().add(new Paragraph(entry.getKey())));
                    categoryTable.addCell(new Cell()
                            .add(new Paragraph(String.format("%.2f €", entry.getValue())))
                            .setTextAlignment(TextAlignment.RIGHT));
                });

        document.add(categoryTable);
    }
}
