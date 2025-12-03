package com.example.finance.service;

import com.example.finance.model.Budget;
import com.example.finance.repository.IBudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final IBudgetRepository repository;

    public BudgetService(IBudgetRepository repository) {
        this.repository = repository;
    }

    public List<Budget> all() {
        return repository.findAll();
    }

    public Budget get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    public int save(Budget budget) {
        validateBudget(budget);
        return repository.save(budget);
    }

    public int delete(Long id) {
        get(id);
        return repository.delete(id);
    }

    private void validateBudget(Budget budget) {
        if (budget.getAmount() <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive");
        }
        if (budget.getPeriod() == null || budget.getPeriod().isEmpty()) {
            throw new IllegalArgumentException("Period is required");
        }
        if (!budget.getPeriod().equals("MONTH") && !budget.getPeriod().equals("YEAR")) {
            throw new IllegalArgumentException("Period must be MONTH or YEAR");
        }
        if (budget.getStartDate() == null || budget.getStartDate().isEmpty()) {
            throw new IllegalArgumentException("Start date is required");
        }
    }
}