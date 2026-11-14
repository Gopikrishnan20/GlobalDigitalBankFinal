package com.gdb.repository;

import com.gdb.domain.Account;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.AtomicFileWriter;
import com.gdb.file.BackupManager;
import com.gdb.file.CsvMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File-backed TransactionRepository storing records in a CSV file.
 * Transfers need the FileAccountRepository that holds the balances.
 */
public class FileTransactionRepository implements TransactionRepository {

    private final String filePath;
    private final boolean backupEnabled;
    private final int maxBackups;
    private final FileAccountRepository accounts;
    private final List<Transaction> cache = new ArrayList<>();

    public FileTransactionRepository(String filePath, FileAccountRepository accounts, boolean backupEnabled, int maxBackups) {
        this.filePath = filePath;
        this.accounts = accounts;
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        load();
    }

    public FileTransactionRepository(String filePath, FileAccountRepository accounts) {
        this(filePath, accounts, true, 5);
    }

    public FileTransactionRepository(String filePath) {
        this(filePath, null, true, 5);
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
                    cache.add(CsvMapper.fromCsvTransaction(lines.get(i)));
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to read " + filePath + ": " + e.getMessage(), e);
        }
    }

    public synchronized void flush() {
        List<String> lines = new ArrayList<>();
        lines.add(CsvMapper.TRANSACTION_HEADER);
        for (Transaction transaction : cache) {
            lines.add(CsvMapper.toCsv(transaction));
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
    public synchronized void save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        cache.add(transaction);
        try {
            flush();
        } catch (RuntimeException e) {
            cache.remove(cache.size() - 1);
            throw e;
        }
    }

    @Override
    public synchronized List<Transaction> findByAccount(int accountNumber) {
        return cache.stream()
                .filter(t -> t.getAccountNumber() == accountNumber
                          || t.getFromAccount() == accountNumber
                          || t.getToAccount() == accountNumber)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public synchronized void clear() {
        cache.clear();
        flush();
    }

    /**
     * 1. Validate and write both new balances to accounts.csv in one atomic write.
     * 2. Append the TRANSFER record to transactions.csv.
     * If step 2 fails, step 1 is undone by writing the old balances back.
     */
    @Override
    public synchronized void transfer(int fromAccountNumber, int toAccountNumber, double amount) {
        if (accounts == null) {
            throw new DataAccessException("Transfer needs a FileAccountRepository; none was configured");
        }
        Account sender = accounts.validateTransfer(fromAccountNumber, toAccountNumber, amount)[0];
        accounts.transfer(fromAccountNumber, toAccountNumber, amount);
        Transaction record = new Transaction(
            Transaction.generateId(), LocalDateTime.now(), fromAccountNumber,
            TransactionType.TRANSFER, amount, sender.getBalance(), "SUCCESS",
            "Transfer of Rs. " + amount + " to Account #" + toAccountNumber,
            fromAccountNumber, toAccountNumber);
        try {
            save(record);
        } catch (RuntimeException e) {
            accounts.transfer(toAccountNumber, fromAccountNumber, amount);
            throw new DataAccessException("Transfer rolled back: " + e.getMessage(), e);
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
