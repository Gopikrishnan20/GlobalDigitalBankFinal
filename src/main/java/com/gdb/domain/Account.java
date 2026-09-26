package com.gdb.domain;

import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InsufficientBalanceException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Account extends AbstractAccount {

    private double dailyTransferTotal;
    private LocalDateTime lastTransferDate = LocalDateTime.now();

    public Account(String accountNumber, String name, int age, double balance,
                   String accountType, String status, String pin) {
        super(accountNumber, name, age, balance, accountType, status, pin);
    }

    public Account(String accountNumber, String name, int age, double balance,
                   String accountType, String status, String pin, int tenureYears) {
        super(accountNumber, name, age, balance, accountType, status, pin, tenureYears);
    }

    @Override
    public void processDebit(double amount) throws AccountException {
        if (amount > getBalance()) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        setBalance(getBalance() - amount);
    }

    public double getDailyTransferLimit() {
        return AccountRulesEngine.getInstance()
                .getDailyTransferLimit(getAccountType(), getTenureYears());
    }

    public double getRemainingDailyTransferLimit() {
        resetDailyTransferIfNeeded();
        return Math.max(0.0, getDailyTransferLimit() - dailyTransferTotal);
    }

    public boolean canTransfer(double amount) {
        resetDailyTransferIfNeeded();
        return dailyTransferTotal + amount <= getDailyTransferLimit();
    }

    public void updateDailyTransferTotal(double amount) {
        resetDailyTransferIfNeeded();
        dailyTransferTotal += amount;
        lastTransferDate = LocalDateTime.now();
    }

    public void resetDailyTransferIfNeeded() {
        if (lastTransferDate == null || !lastTransferDate.toLocalDate().equals(LocalDate.now())) {
            dailyTransferTotal = 0.0;
            lastTransferDate = LocalDateTime.now();
        }
    }

    public double getDailyTransferTotal() {
        return dailyTransferTotal;
    }

    public LocalDateTime getLastTransferDate() {
        return lastTransferDate;
    }

    public void suspend() {
        setStatus("SUSPENDED");
    }

    public void activate() {
        setStatus("ACTIVE");
    }

    public void close() {
        setStatus("CLOSED");
    }
}