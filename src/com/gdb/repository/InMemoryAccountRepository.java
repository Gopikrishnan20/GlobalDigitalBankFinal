package com.gdb.repository;

import com.gdb.domain.Account;
import com.gdb.domain.IAccount;
import com.gdb.exceptions.DataAccessException;
import java.util.*;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Integer, IAccount> storage = new HashMap<>();
    private int nextAccountNumber = 1001;

    public InMemoryAccountRepository() {
    }

    @Override
    public synchronized void save(IAccount account) {
        if (account != null) {
            storage.put(account.getAccountNumber(), account);
        }
    }

    @Override
    public synchronized IAccount findById(int accountNumber) {
        return storage.get(accountNumber);
    }

    @Override
    public synchronized List<IAccount> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized void update(IAccount account) {
        if (account != null && storage.containsKey(account.getAccountNumber())) {
            storage.put(account.getAccountNumber(), account);
        }
    }

    @Override
    public synchronized void delete(int accountNumber) {
        storage.remove(accountNumber);
    }

    @Override
    public synchronized boolean exists(int accountNumber) {
        return storage.containsKey(accountNumber);
    }

    @Override
    public synchronized int nextAccountNumber() {
        return nextAccountNumber++;
    }

    @Override
    public synchronized void transfer(int fromAccount, int toAccount, double amount) {
        IAccount from = storage.get(fromAccount);
        IAccount to = storage.get(toAccount);
        if (from == null || to == null) {
            throw new DataAccessException("Transfer failed: account not found");
        }
        if (!to.isActive()) {
            throw new DataAccessException("Transfer failed: destination account is inactive");
        }
        if (amount <= 0 || from.getBalance() < amount) {
            throw new DataAccessException("Transfer failed: invalid amount or insufficient balance");
        }
        try {
            ((Account) from).withdraw(amount, ((Account) from).getPin() == null ? 0 : ((Account) from).getPin());
            to.deposit(amount);
        } catch (Exception e) {
            throw new DataAccessException("Transfer failed: " + e.getMessage(), e);
        }
    }
}
