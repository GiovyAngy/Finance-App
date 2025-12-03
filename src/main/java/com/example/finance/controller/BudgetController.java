package com.example.finance.controller;

import com.example.finance.model.Budget;
import com.example.finance.service.BudgetService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin
public class BudgetController {
    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @GetMapping
    public List<Budget> all() {
        return service.all();
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Budget b) {
        service.save(b);
        return new ResponseEntity<>("ok", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Budget b) {
        b.setId(id);
        service.save(b);
        return ResponseEntity.ok("updated");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("deleted");
    }
}
