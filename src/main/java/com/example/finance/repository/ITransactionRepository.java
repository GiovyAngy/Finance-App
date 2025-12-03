package com.example.finance.repository;

import com.example.finance.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface ITransactionRepository {
    List<Transaction> findAll();
    Optional<Transaction> findById(Long id);
    int save(Transaction transaction);
    int delete(Long id);
}