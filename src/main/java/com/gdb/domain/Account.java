package com.gdb.domain;

import com.gdb.exceptions.*;

public class Account {
    
    private String accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private String pin;
  
    public Account(String accountNumber, String name, int age, double balance, 
                   String accountType, String status, String pin) {
        // Validate age
        if (age < 18) {
            throw new IllegalArgumentException("Customer age must be at least 18 years old.");
        }
        
        // Validate balance
        if (balance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        
        // Validate PIN (exactly 4 digits)
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
    }
    
    
    public boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;
        }
        return false;
    }
    

    public boolean withdraw(double amount, String enteredPin) throws InvalidPinException, 
                                                                   InactiveAccountException,
                                                                   InvalidAmountException,
                                                                   InsufficientBalanceException {
        // Verify PIN
        if (!validatePin(enteredPin)) {
            throw new InvalidPinException("Invalid PIN entered");
        }
        
        // Check account status
        if (!status.equals("ACTIVE")) {
            throw new InactiveAccountException("Account is not active");
        }
        
        // Check amount validity
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        
        // Check balance
        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        
        // Perform withdrawal
        balance -= amount;
        return true;
    }
    
    
    public boolean validatePin(String enteredPin) {
        if (enteredPin == null || enteredPin.isEmpty()) {
            return false;
        }
        return this.pin.equals(enteredPin);
    }
    
    public boolean changePin(String oldPin, String newPin) {
        // Validate old PIN
        if (!validatePin(oldPin)) {
            return false;
        }
        
        // Validate new PIN
        if (!isValidPin(newPin)) {
            return false;
        }
        
        // Update PIN
        this.pin = newPin;
        return true;
    }
    
    private boolean isValidPin(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        // Check if all characters are digits
        return pin.matches("\\d{4}");
    }

    public void suspend() {
        this.status = "SUSPENDED";
    }

    public void activate() {
        this.status = "ACTIVE";
    }
    

    public void close() {
        this.status = "CLOSED";
    }
    

    public void displayAccountInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Balance: Rs " + balance);
        System.out.println("Account Type: " + accountType);
        System.out.println("Status: " + status);
    }
    

    public String getAccountNumber() {
        return accountNumber;
    }
    

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
    

    public double getBalance() {
        return balance;
    }
    

    public String getAccountType() {
        return accountType;
    }
    

    public String getStatus() {
        return status;
    }
    

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
