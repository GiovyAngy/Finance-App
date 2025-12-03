package com.example.finance.controller;

import com.example.finance.model.Transaction;
import com.example.finance.repository.ITransactionRepository;
import com.example.finance.service.TransactionService;
import com.example.finance.service.ExportService;
import com.example.finance.service.ImportService;
import com.example.finance.repository.ITransactionRepository;
import com.itextpdf.io.source.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;
    private final ExportService exportService;
    private final ImportService importService;
    private final ITransactionRepository repository;

    public TransactionController(
            TransactionService transactionService,
            @Qualifier("excelExportService") ExportService exportService,
            @Qualifier("excelImportService") ImportService importService, ITransactionRepository repository) {
        this.transactionService = transactionService;
        this.exportService = exportService;
        this.importService = importService;
        this.repository = repository;
    }

    @GetMapping
    public List<Transaction> all() {
        return transactionService.all();
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable Long id) {
        return transactionService.get(id);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction t) {
        transactionService.save(t);
        return new ResponseEntity<>(t, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Transaction t) {
        t.setId(id);
        transactionService.save(t);
        return ResponseEntity.ok("updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok("deleted");
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType(exportService.getContentType());
        response.setHeader("Content-Disposition",
                "attachment; filename=" + exportService.getDefaultFileName());

        List<Transaction> transactions = transactionService.all();
        exportService.export(transactions, response.getOutputStream());
    }

   
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importFile(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<Transaction> transactions = importService.importData(file.getInputStream());

            
            for (Transaction t : transactions) {
                transactionService.save(t);
            }

            response.put("success", true);
            response.put("message", "imported " + transactions.size() + " transactions");
            response.put("count", transactions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Import failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Autowired
    @Qualifier("pdfExportService")
    private ExportService pdfExportService;

    @Autowired
    @Qualifier("pdfImportService")
    private ImportService pdfImportService;

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        try {
            List<Transaction> transactions = repository.findAll();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            pdfExportService.export(transactions, baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(pdfExportService.getContentType()));
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(pdfExportService.getDefaultFileName())
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import/pdf")
    public ResponseEntity<Map<String, Object>> importPdf(@RequestParam("file") MultipartFile file) {
        try {
            List<Transaction> imported = pdfImportService.importData(file.getInputStream());

            for (Transaction t : imported) {
                transactionService.save(t);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", imported.size() + " Transaktionen erfolgreich importiert");
            response.put("count", imported.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Import fehlgeschlagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}