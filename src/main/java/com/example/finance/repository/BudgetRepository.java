package com.example.finance.repository;

import com.example.finance.model.Budget;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BudgetRepository implements IBudgetRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Budget> mapper = (rs, rowNum) -> {
        Budget b = new Budget();
        b.setId(rs.getLong("id"));
        b.setPeriod(rs.getString("period"));
        b.setAmount(rs.getDouble("amount"));
        b.setStartDate(rs.getString("start_date"));
        b.setName(rs.getString("name"));
        return b;
    };

    public BudgetRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Budget> findAll() {
        return jdbc.query("SELECT * FROM budgets ORDER BY start_date DESC", mapper);
    }

    @Override
    public Optional<Budget> findById(Long id) {
        try {
            Budget budget = jdbc.queryForObject(
                    "SELECT * FROM budgets WHERE id = ?",
                    new Object[]{id},
                    mapper
            );
            return Optional.ofNullable(budget);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public int save(Budget b) {
        if (b.getId() == null) {
            
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int result = jdbc.update((Connection con) -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO budgets(period,amount,start_date,name) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, b.getPeriod());
                ps.setDouble(2, b.getAmount());
                ps.setString(3, b.getStartDate());
                ps.setString(4, b.getName());
                return ps;
            }, keyHolder);

           
            if (keyHolder.getKey() != null) {
                b.setId(keyHolder.getKey().longValue());
            }

            return result;
        } else {
            
            return jdbc.update(
                    "UPDATE budgets SET period=?,amount=?,start_date=?,name=? WHERE id=?",
                    b.getPeriod(), b.getAmount(), b.getStartDate(), b.getName(), b.getId()
            );
        }
    }

    @Override
    public int delete(Long id) {
        return jdbc.update("DELETE FROM budgets WHERE id=?", id);
    }
}