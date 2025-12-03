package com.example.finance.service;


import com.example.finance.model.Transaction;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("pdfImportService")
public class PdfImportService implements ImportService {

    // Pattern per estrarre dati dal PDF
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "(\\d+)\\s+([\\d.-]+)\\s+(EINNAHMEN|AUSGABEN)\\s+([^\\s]+)\\s+(.+?)\\s+([\\d.,-]+)\\s*€"
    );

    @Override
    public List<Transaction> importData(InputStream inputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try {
            PdfReader reader = new PdfReader(inputStream);
            PdfDocument pdfDoc = new PdfDocument(reader);

            // Estrai testo da tutte le pagine
            StringBuilder fullText = new StringBuilder();
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                fullText.append(pageText).append("\n");
            }

            pdfDoc.close();

            // Parse del testo estratto
            transactions = parseTransactionsFromText(fullText.toString());

        } catch (Exception e) {
            throw new IOException("Fehler beim PDF-Import: " + e.getMessage(), e);
        }

        return transactions;
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("pdf");
    }

    private List<Transaction> parseTransactionsFromText(String text) {
        List<Transaction> transactions = new ArrayList<>();
        Matcher matcher = TRANSACTION_PATTERN.matcher(text);

        while (matcher.find()) {
            try {
                Transaction t = new Transaction();
                // Non impostiamo l'ID perché sarà generato dal database
                t.setDate(matcher.group(2));
                t.setType(matcher.group(3));
                t.setCategory(matcher.group(4));
                t.setDescription(matcher.group(5).trim());

                // Parse dell'importo (gestisce sia punto che virgola)
                String amountStr = matcher.group(6).replace(",", ".");
                t.setAmount(Double.parseDouble(amountStr));

                transactions.add(t);
            } catch (Exception e) {
                System.err.println("Fehler beim Parsen der Zeile: " + matcher.group(0));
            }
        }

        return transactions;
    }
}