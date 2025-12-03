package com.example.finance.service;

import com.example.finance.model.Transaction;
import com.example.finance.repository.ITransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TransactionService {

    private final ITransactionRepository repository;

    public TransactionService(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> all() {
        return repository.findAll();
    }

    public Transaction get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    public int save(Transaction transaction) {
        validateTransaction(transaction);
        return repository.save(transaction);
    }

    public int delete(Long id) {
        get(id);
        return repository.delete(id);
    }


    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == 0) {
            throw new IllegalArgumentException("Amount cannot be zero");
        }
        if (transaction.getDate() == null || transaction.getDate().isEmpty()) {
            throw new IllegalArgumentException("Date is required");
        }
        if (transaction.getType() == null || transaction.getType().isEmpty()) {
            throw new IllegalArgumentException("Type is required");
        }

        String type = transaction.getType().toUpperCase();

        if (!type.equals("EINNAHMEN") && !type.equals("AUSGABEN")) {
            throw new IllegalArgumentException("Type must be EINNAHMEN or AUSGABEN");
        }
    }

}