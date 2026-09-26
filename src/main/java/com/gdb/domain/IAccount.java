package com.gdb.domain;

import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidAmountException;

public interface IAccount {
    String getAccountNumber();

    String getName();

    default String getCustomerName() {
        return getName();
    }

    double getBalance();

    String getAccountType();

    String getStatus();

    default boolean verifyPin(String pin) {
        return false;
    }

    default boolean canWithdraw(double amount) {
        return amount > 0 && amount <= getBalance();
    }

    boolean deposit(double amount) throws InvalidAmountException;

    boolean withdraw(double amount, String pin) throws AccountException;

    void displayAccountInfo();
}
