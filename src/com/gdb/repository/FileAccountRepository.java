package com.gdb.repository;

import com.gdb.domain.Account;
import com.gdb.domain.IAccount;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.AtomicFileWriter;
import com.gdb.file.BackupManager;
import com.gdb.file.CsvMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * File-backed AccountRepository. Accounts are kept in an in-memory cache and written to a CSV file
 * with AtomicFileWriter after every change; the previous file is backed up first.
 */
public class FileAccountRepository implements AccountRepository {

    private final String filePath;
    private final boolean backupEnabled;
    private final int maxBackups;
    private final Map<Integer, IAccount> cache = new LinkedHashMap<>();

    public FileAccountRepository(String filePath, boolean backupEnabled, int maxBackups) {
        this.filePath = filePath;
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        load();
    }

    public FileAccountRepository(String filePath) {
        this(filePath, true, 5);
    }

    public synchronized void load() {
        cache.clear();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {          // line 0 is the header
                if (!lines.get(i).trim().isEmpty()) {
                    IAccount account = CsvMapper.fromCsv(lines.get(i));
                    cache.put(account.getAccountNumber(), account);
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read " + filePath + ": " + e.getMessage(), e);
        }
    }

    public synchronized void flush() {
        List<String> lines = new ArrayList<>();
        lines.add(CsvMapper.ACCOUNT_HEADER);
        for (IAccount account : cache.values()) {
            lines.add(CsvMapper.toCsv(account));
        }
        try {
            if (backupEnabled) {
                BackupManager.backup(filePath, maxBackups);
            }
            AtomicFileWriter.write(filePath, lines);
        } catch (IOException e) {
            throw new DataAccessException("Failed to write " + filePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void save(IAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (cache.containsKey(account.getAccountNumber())) {
            throw new DataAccessException("Account " + account.getAccountNumber() + " already exists");
        }
        cache.put(account.getAccountNumber(), account);
        flush();
    }

    @Override
    public synchronized IAccount findById(int accountNumber) {
        return cache.get(accountNumber);
    }

    @Override
    public synchronized List<IAccount> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized void update(IAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (!cache.containsKey(account.getAccountNumber())) {
            throw new DataAccessException("Account " + account.getAccountNumber() + " not found");
        }
        cache.put(account.getAccountNumber(), account);
        flush();
    }

    @Override
    public synchronized void delete(int accountNumber) {
        if (cache.remove(accountNumber) != null) {
            flush();
        }
    }

    @Override
    public synchronized boolean exists(int accountNumber) {
        return cache.containsKey(accountNumber);
    }

    @Override
    public synchronized int nextAccountNumber() {
        int max = 1000;
        for (int accountNumber : cache.keySet()) {
            max = Math.max(max, accountNumber);
        }
        return max + 1;
    }

    /**
     * Moves money between two accounts and writes both new balances in a single atomic file write.
     * If the write fails, the in-memory balances are put back, so cache and file stay identical.
     */
    @Override
    public synchronized void transfer(int fromAccount, int toAccount, double amount) {
        Account[] pair = validateTransfer(fromAccount, toAccount, amount);
        double fromBefore = pair[0].getBalance();
        double toBefore = pair[1].getBalance();
        pair[0].restoreState(fromBefore - amount, null);
        pair[1].restoreState(toBefore + amount, null);
        try {
            flush();
        } catch (RuntimeException e) {
            pair[0].restoreState(fromBefore, null);
            pair[1].restoreState(toBefore, null);
            throw e;
        }
    }

    /** Checks a transfer and returns {sender, receiver}; throws DataAccessException if it is not allowed. */
    synchronized Account[] validateTransfer(int fromAccount, int toAccount, double amount) {
        if (amount <= 0) {
            throw new DataAccessException("Transfer amount must be positive. Provided: Rs. " + amount);
        }
        if (fromAccount == toAccount) {
            throw new DataAccessException("Cannot transfer to the same account");
        }
        IAccount from = cache.get(fromAccount);
        IAccount to = cache.get(toAccount);
        if (from == null) throw new DataAccessException("Account " + fromAccount + " not found");
        if (to == null) throw new DataAccessException("Account " + toAccount + " not found");
        if (!from.isActive()) throw new DataAccessException("Account " + fromAccount + " is not active");
        if (!to.isActive()) throw new DataAccessException("Account " + toAccount + " is not active");
        if (from.getBalance() < amount) {
            throw new DataAccessException("Insufficient balance in account " + fromAccount
                + ": Rs. " + from.getBalance() + " < Rs. " + amount);
        }
        return new Account[] { (Account) from, (Account) to };
    }

    public String getFilePath() {
        return filePath;
    }
}
