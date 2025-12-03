package com.example.finance.repository;

import com.example.finance.model.Transaction;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;


@Repository
public class TransactionRepository implements ITransactionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Transaction> mapper = (rs, rowNum) -> {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setAmount(rs.getDouble("amount"));
        t.setType(rs.getString("type"));
        t.setCategory(rs.getString("category"));
        t.setDescription(rs.getString("description"));
        t.setDate(rs.getString("date"));
        return t;
    };

    public TransactionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query("SELECT * FROM transactions ORDER BY date DESC", mapper);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        try {
            Transaction transaction = jdbc.queryForObject(
                    "SELECT * FROM transactions WHERE id = ?",
                    new Object[]{id},
                    mapper
            );
            return Optional.ofNullable(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

   
    @Override
    public int save(Transaction t) {
        if (t.getId() == null) {
           
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int result = jdbc.update((Connection con) -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO transactions(amount,type,category,description,date) VALUES(?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setDouble(1, t.getAmount());
                ps.setString(2, t.getType());
                ps.setString(3, t.getCategory());
                ps.setString(4, t.getDescription());
                ps.setString(5, t.getDate());
                return ps;
            }, keyHolder);

           
            if (keyHolder.getKey() != null) {
                t.setId(keyHolder.getKey().longValue());
            }

            return result;
        } else {
           
            return jdbc.update(
                    "UPDATE transactions SET amount=?,type=?,category=?,description=?,date=? WHERE id=?",
                    t.getAmount(), t.getType(), t.getCategory(), t.getDescription(), t.getDate(), t.getId()
            );
        }
    }

    @Override
    public int delete(Long id) {
        return jdbc.update("DELETE FROM transactions WHERE id=?", id);
    }
}