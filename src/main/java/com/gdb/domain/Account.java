package com.gdb.domain;

import com.gdb.exceptions.*;

public class Account extends AbstractAccount {
  
    public Account(String accountNumber, String name, int age, double balance, 
                   String accountType, String status, String pin) {
        super(accountNumber, name, age, balance, accountType, status, pin);
    }
    @Override
    public void processDebit(double amount) throws AccountException {
        if (amount > getBalance()) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        setBalance(getBalance() - amount);
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
