package com.gdb.repository;

import com.gdb.domain.IAccount;
import java.util.*;

/**
 * In-memory implementation of AccountRepository backed by a Map.
 */
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Integer, IAccount> accounts = new HashMap<>();
    private int nextAccountNumber = 1001;

    @Override
    public synchronized void save(IAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        int accountNumber = account.getAccountNumber();
        accounts.put(accountNumber, account);
        nextAccountNumber = Math.max(nextAccountNumber, accountNumber + 1);
    }

    @Override
    public synchronized IAccount findById(int accountNumber) {
        return accounts.get(accountNumber);
    }

    @Override
    public synchronized List<IAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public synchronized void update(IAccount account) {
        Objects.requireNonNull(account, "account must not be null");
        int accountNumber = account.getAccountNumber();
        if (accounts.containsKey(accountNumber)) {
            accounts.put(accountNumber, account);
        }
    }

    @Override
    public synchronized void delete(int accountNumber) {
        accounts.remove(accountNumber);
    }

    @Override
    public synchronized boolean exists(int accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    @Override
    public synchronized int nextAccountNumber() {
        return nextAccountNumber++;
    }
}
