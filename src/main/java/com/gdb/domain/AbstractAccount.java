package com.gdb.domain;

import com.gdb.exceptions.*;

public abstract class AbstractAccount implements IAccount {
    private String accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private String pin;
    private int tenureYears;

    protected AbstractAccount(String accountNumber, String name, int age, double balance,
                              String accountType, String status, String pin) {
        this(accountNumber, name, age, balance, accountType, status, pin, 0);
    }

    protected AbstractAccount(String accountNumber, String name, int age, double balance,
                              String accountType, String status, String pin, int tenureYears) {
        if (age < 18) {
            throw new IllegalArgumentException("Customer age must be at least 18 years old.");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        if (!isValidPin(pin)) {
            throw new IllegalArgumentException("PIN must be exactly 4 numerical digits.");
        }
        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = balance;
        this.accountType = accountType;
        this.status = status;
        this.pin = pin;
        this.tenureYears = tenureYears;
    }

    @Override
    public boolean deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }
        balance += amount;
        return true;
    }

    public boolean validatePin(String enteredPin) {
        return enteredPin != null && !enteredPin.isEmpty() && pin.equals(enteredPin);
    }

    @Override
    public boolean verifyPin(String enteredPin) {
        return validatePin(enteredPin);
    }

    @Override
    public boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }

    public boolean changePin(String oldPin, String newPin) {
        if (!validatePin(oldPin) || !isValidPin(newPin)) {
            return false;
        }
        pin = newPin;
        return true;
    }

    @Override
    public boolean withdraw(double amount, String enteredPin) throws InvalidPinException,
                                                                      InactiveAccountException,
                                                                      InvalidAmountException,
                                                                      AccountException {
        if (!validatePin(enteredPin)) {
            throw new InvalidPinException("Invalid PIN entered");
        }
        if (!"ACTIVE".equals(status)) {
            if ("CLOSED".equals(status)) {
                throw new InactiveAccountException("Account is closed");
            }
            throw new InactiveAccountException("Account is not active");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        processDebit(amount);
        return true;
    }

    private boolean isValidPin(String value) {
        return value != null && value.matches("\\d{4}");
    }

    @Override
    public void displayAccountInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Balance: Rs " + balance);
        System.out.println("Account Type: " + accountType);
        System.out.println("Status: " + status);
    }

    public abstract void processDebit(double amount) throws AccountException;

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public String getStatus() { return status; }
    public int getTenureYears() { return tenureYears; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setStatus(String status) { this.status = status; }
    public void setTenureYears(int tenureYears) { this.tenureYears = tenureYears; }
}
