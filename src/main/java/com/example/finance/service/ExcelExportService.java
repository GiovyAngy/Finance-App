package com.example.finance.service;

import com.example.finance.model.Transaction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service("excelExportService")
public class ExcelExportService implements ExportService {

    @Override
    public void export(List<Transaction> transactions, OutputStream outputStream) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            createTransactionsSheet(wb, transactions);
            createChartsSheet(wb, transactions);
            wb.write(outputStream);
        }
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getDefaultFileName() {
        return "finance_export.xlsx";
    }

    private void createTransactionsSheet(XSSFWorkbook wb, List<Transaction> transactions) {
        XSSFSheet sheet = wb.createSheet("transactions");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Date");
        header.createCell(2).setCellValue("Type");
        header.createCell(3).setCellValue("Category");
        header.createCell(4).setCellValue("Description");
        header.createCell(5).setCellValue("Amount");

        int rowIndex = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(t.getId() != null ? t.getId() : 0);
            row.createCell(1).setCellValue(t.getDate());
            row.createCell(2).setCellValue(t.getType());
            row.createCell(3).setCellValue(t.getCategory() != null ? t.getCategory() : "");
            row.createCell(4).setCellValue(t.getDescription() != null ? t.getDescription() : "");
            row.createCell(5).setCellValue(t.getAmount());
        }
    }

    private void createChartsSheet(XSSFWorkbook wb, List<Transaction> transactions) {
        XSSFSheet chartSheet = wb.createSheet("Grafiken");
        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();

        createBalanceChart(chartSheet, drawing, transactions);
    }

    private void createBalanceChart(XSSFSheet chartSheet, XSSFDrawing drawing, List<Transaction> transactions) {
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 9, 0, 17, 20);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Kontostand Verlauf");
        chart.setTitleOverlay(false);

        XDDFCategoryAxis dateAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        dateAxis.setTitle("Datum");

        XDDFValueAxis saldoAxis = chart.createValueAxis(AxisPosition.LEFT);
        saldoAxis.setTitle("Kontostand");

        List<String> dates = new ArrayList<>();
        List<Double> saldo = new ArrayList<>();
        double running = 0;

        for (Transaction t : transactions) {
            running += t.getAmount();
            dates.add(t.getDate());
            saldo.add(running);
        }

        int startRow = 40;
        for (int i = 0; i < dates.size(); i++) {
            Row r = chartSheet.createRow(startRow + i);
            r.createCell(0).setCellValue(dates.get(i));
            r.createCell(1).setCellValue(saldo.get(i));
        }

        XDDFDataSource<String> datesSrc = XDDFDataSourcesFactory.fromStringCellRange(
                chartSheet, new CellRangeAddress(startRow, startRow + dates.size() - 1, 0, 0)
        );

        XDDFNumericalDataSource<Double> saldoSrc = XDDFDataSourcesFactory.fromNumericCellRange(
                chartSheet, new CellRangeAddress(startRow, startRow + dates.size() - 1, 1, 1)
        );

        XDDFChartData lineData = chart.createData(ChartTypes.LINE, dateAxis, saldoAxis);
        lineData.addSeries(datesSrc, saldoSrc).setTitle("Kontostand", null);
        chart.plot(lineData);
    }
}