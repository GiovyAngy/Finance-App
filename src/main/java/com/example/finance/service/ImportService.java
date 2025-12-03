package com.example.finance.service;

import com.example.finance.model.Transaction;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

public interface ImportService {
    List<Transaction> importData(InputStream inputStream) throws IOException;
    List<String> getSupportedFormats();
}