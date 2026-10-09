package com.gdb.repository;

import com.gdb.domain.Transaction;
import java.util.*;

/**
 * In-memory implementation of TransactionRepository backed by a List.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public synchronized void save(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
        }
    }

    @Override
    public synchronized List<Transaction> findByAccount(int accountNumber) {
        List<Transaction> matchingTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getAccountNumber() == accountNumber
                    || transaction.getFromAccount() == accountNumber
                    || transaction.getToAccount() == accountNumber) {
                matchingTransactions.add(transaction);
            }
        }
        return matchingTransactions;
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(transactions);
    }

    @Override
    public synchronized void clear() {
        transactions.clear();
    }
}
