package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed implementation of TransactionRepository.
 * Handles transaction record persistence, retrieval, and multi-account atomic transfers.
 */
public class JdbcTransactionRepository implements TransactionRepository {

    private static final String INSERT_SQL =
        "INSERT INTO transactions (transaction_id, timestamp, account_number, type, amount, balance_after, " +
        "status, description, from_account, to_account) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ACCOUNT_SQL =
        "SELECT * FROM transactions WHERE account_number = ? OR from_account = ? OR to_account = ? ORDER BY timestamp ASC";
    private static final String SELECT_ALL_SQL = "SELECT * FROM transactions ORDER BY timestamp ASC";
    private static final String DELETE_ALL_SQL = "DELETE FROM transactions";
    private static final String BALANCE_STATUS_SQL = "SELECT balance, status FROM accounts WHERE account_number = ?";
    private static final String DEBIT_SQL = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
    private static final String CREDIT_SQL = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

    private final ConnectionProvider provider;

    public JdbcTransactionRepository(ConnectionProvider provider) {
        this.provider = provider;
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        try (Connection conn = provider.getConnection()) {
            insert(conn, transaction);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save transaction " + transaction.getTransactionId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findByAccount(int accountNumber) {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ACCOUNT_SQL)) {
            ps.setInt(1, accountNumber);
            ps.setInt(2, accountNumber);
            ps.setInt(3, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load transactions for account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            return mapAll(rs);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ALL_SQL)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public void transfer(int fromAccountNumber, int toAccountNumber, double amount) {
        if (amount <= 0) {
            throw new DataAccessException("Transfer amount must be positive. Provided: Rs. " + amount);
        }
        if (fromAccountNumber == toAccountNumber) {
            throw new DataAccessException("Cannot transfer to the same account");
        }
        try (Connection conn = provider.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                double fromBalance = requireActiveBalance(conn, fromAccountNumber);
                requireActiveBalance(conn, toAccountNumber);
                if (fromBalance < amount) {
                    throw new DataAccessException("Insufficient balance in account " + fromAccountNumber
                        + ": Rs. " + fromBalance + " < Rs. " + amount);
                }
                updateBalance(conn, DEBIT_SQL, fromAccountNumber, amount);
                updateBalance(conn, CREDIT_SQL, toAccountNumber, amount);
                insert(conn, new Transaction(
                    Transaction.generateId(), LocalDateTime.now(), fromAccountNumber,
                    TransactionType.TRANSFER, amount, fromBalance - amount, "SUCCESS",
                    "Transfer of Rs. " + amount + " to Account #" + toAccountNumber,
                    fromAccountNumber, toAccountNumber));
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                if (e instanceof DataAccessException) throw (DataAccessException) e;
                throw new DataAccessException("Transfer rolled back: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transfer failed: " + e.getMessage(), e);
        }
    }

    private void insert(Connection conn, Transaction t) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, (t.getTimestamp() != null ? t.getTimestamp() : LocalDateTime.now()).toString());
            ps.setInt(3, t.getAccountNumber());
            ps.setString(4, t.getType().name());
            ps.setDouble(5, t.getAmount());
            ps.setDouble(6, t.getBalanceAfter());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getDescription());
            ps.setInt(9, t.getFromAccount());
            ps.setInt(10, t.getToAccount());
            ps.executeUpdate();
        }
    }

    private double requireActiveBalance(Connection conn, int accountNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(BALANCE_STATUS_SQL)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Account " + accountNumber + " not found");
                }
                if (!"Active".equalsIgnoreCase(rs.getString("status"))) {
                    throw new DataAccessException("Account " + accountNumber + " is not active");
                }
                return rs.getDouble("balance");
            }
        }
    }

    private void updateBalance(Connection conn, String sql, int accountNumber, double amount) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, accountNumber);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Balance update affected no rows for account " + accountNumber);
            }
        }
    }

    private List<Transaction> mapAll(ResultSet rs) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(mapRow(rs));
        }
        return transactions;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        String timestamp = rs.getString("timestamp");
        return new Transaction(
            rs.getString("transaction_id"),
            timestamp != null ? LocalDateTime.parse(timestamp) : LocalDateTime.now(),
            rs.getInt("account_number"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getDouble("amount"),
            rs.getDouble("balance_after"),
            rs.getString("status"),
            rs.getString("description"),
            rs.getInt("from_account"),
            rs.getInt("to_account"));
    }
}
