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

    boolean deposit(double amount) throws InvalidAmountException;

    boolean withdraw(double amount, String pin) throws AccountException;

    void displayAccountInfo();
}
