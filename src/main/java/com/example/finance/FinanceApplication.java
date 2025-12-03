package com.example.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }

    @Bean
    public boolean initializeDatabase(JdbcTemplate jdbc) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL NOT NULL, type TEXT NOT NULL, category TEXT, description TEXT, date TEXT NOT NULL)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, period TEXT NOT NULL, amount REAL NOT NULL, start_date TEXT, name TEXT)");
        return true;
    }
}