package com.example.finance.service;

import com.example.finance.model.Transaction;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

public interface ExportService {
    void export(List<Transaction> transactions, OutputStream outputStream) throws IOException;
    String getContentType();
    String getDefaultFileName();
}