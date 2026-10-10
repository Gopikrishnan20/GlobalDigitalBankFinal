package com.gdb.repository;

import com.gdb.domain.IAccount;
import java.util.List;

/**
 * Repository interface for account data access and persistence operations.
 * Decouples domain and service layers from concrete storage implementations.
 */
public interface AccountRepository {
    void save(IAccount account);
    IAccount findById(int accountNumber);
    List<IAccount> findAll();
    void update(IAccount account);
    void delete(int accountNumber);
    boolean exists(int accountNumber);
    int nextAccountNumber();

    /** Atomically debits fromAccount and credits toAccount; rolls back on any failure. */
    void transfer(int fromAccount, int toAccount, double amount);
}
