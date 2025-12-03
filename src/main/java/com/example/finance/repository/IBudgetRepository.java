package com.example.finance.repository;

import com.example.finance.model.Budget;
import java.util.List;
import java.util.Optional;

public interface IBudgetRepository {
    List<Budget> findAll();
    Optional<Budget> findById(Long id);
    int save(Budget budget);
    int delete(Long id);
}