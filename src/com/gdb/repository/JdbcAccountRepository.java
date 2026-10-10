package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.domain.*;
import com.gdb.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAccountRepository implements AccountRepository {

    private static final String INSERT_SQL =
        "INSERT INTO accounts (account_number, account_holder_name, age, balance, account_type, status, pin, opening_date, tenure_years) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM accounts WHERE account_number = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM accounts ORDER BY account_number";
    private static final String UPDATE_SQL =
        "UPDATE accounts SET account_holder_name = ?, balance = ?, status = ?, pin = ?, tenure_years = ? WHERE account_number = ?";
    private static final String DELETE_SQL = "DELETE FROM accounts WHERE account_number = ?";
    private static final String EXISTS_SQL = "SELECT 1 FROM accounts WHERE account_number = ?";
    private static final String NEXT_ID_SQL = "SELECT COALESCE(MAX(account_number), 1000) + 1 AS next_id FROM accounts";
    private static final String BALANCE_STATUS_SQL = "SELECT balance, status FROM accounts WHERE account_number = ?";
    private static final String ADJUST_BALANCE_SQL = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";

    private final ConnectionProvider provider;

    public JdbcAccountRepository(ConnectionProvider provider) {
        this.provider = provider;
    }

    @Override
    public void save(IAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, account.getAccountNumber());
            ps.setString(2, account.getAccountHolderName());
            ps.setInt(3, ageOf(account));
            ps.setDouble(4, account.getBalance());
            ps.setString(5, account.getAccountType());
            ps.setString(6, account.isActive() ? "Active" : "Closed");
            bindPin(ps, 7, account);
            ps.setString(8, account.getOpeningDate());
            ps.setInt(9, account.getTenureYears());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save account " + account.getAccountNumber() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public IAccount findById(int accountNumber) {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<IAccount> findAll() {
        List<IAccount> accounts = new ArrayList<>();
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(IAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, account.getAccountHolderName());
            ps.setDouble(2, account.getBalance());
            ps.setString(3, account.isActive() ? "Active" : "Closed");
            bindPin(ps, 4, account);
            ps.setInt(5, account.getTenureYears());
            ps.setInt(6, account.getAccountNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update account " + account.getAccountNumber() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int accountNumber) {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(int accountNumber) {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_SQL)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int nextAccountNumber() {
        try (Connection conn = provider.getConnection();
             PreparedStatement ps = conn.prepareStatement(NEXT_ID_SQL);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("next_id") : 1001;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate next account number: " + e.getMessage(), e);
        }
    }

    /**
     * Moves funds between two accounts inside a single JDBC transaction.
     * Either both the debit and the credit are committed, or neither is.
     */
    @Override
    public void transfer(int fromAccount, int toAccount, double amount) {
        if (amount <= 0) {
            throw new DataAccessException("Transfer amount must be positive. Provided: Rs. " + amount);
        }
        if (fromAccount == toAccount) {
            throw new DataAccessException("Cannot transfer to the same account");
        }
        try (Connection conn = provider.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                double fromBalance = requireActiveBalance(conn, fromAccount);
                requireActiveBalance(conn, toAccount);
                if (fromBalance < amount) {
                    throw new DataAccessException("Insufficient balance in account " + fromAccount
                        + ": Rs. " + fromBalance + " < Rs. " + amount);
                }
                adjustBalance(conn, fromAccount, -amount);
                adjustBalance(conn, toAccount, amount);
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

    private void adjustBalance(Connection conn, int accountNumber, double delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(ADJUST_BALANCE_SQL)) {
            ps.setDouble(1, delta);
            ps.setInt(2, accountNumber);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Balance update affected no rows for account " + accountNumber);
            }
        }
    }

    private static int ageOf(IAccount account) {
        return account instanceof Account ? ((Account) account).getAge() : 18;
    }

    private static void bindPin(PreparedStatement ps, int index, IAccount account) throws SQLException {
        Integer pin = account instanceof Account ? ((Account) account).getPin() : null;
        if (pin == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, pin);
    }

    public IAccount mapRow(ResultSet rs) throws SQLException {
        int accNo = rs.getInt("account_number");
        String name = rs.getString("account_holder_name");
        int age = rs.getInt("age");
        double balance = rs.getDouble("balance");
        String type = rs.getString("account_type");
        String status = rs.getString("status");
        int pin = rs.getInt("pin");
        int tenure = rs.getInt("tenure_years");

        try {
            Account acc = (Account) AccountFactory.createAccount(type, accNo, name, Math.max(18, age), balance, tenure);
            if (pin > 0) {
                try { acc.setPin(pin); } catch (Exception ignored) {}
            }
            if (!"Active".equalsIgnoreCase(status)) {
                try { acc.closeAccount(); } catch (Exception ignored) {}
            }
            return acc;
        } catch (Exception e) {
            throw new SQLException("Error mapping row to Account: " + e.getMessage(), e);
        }
    }
}
