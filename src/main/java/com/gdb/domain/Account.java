package com.gdb.domain;


public class Account {
    
    private String accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private String pin;
    
    /**
     * Constructor with PIN and validations.
     * Validates age (>= 18), balance (>= 0), and PIN (exactly 4 digits).
     * 
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age (must be >= 18)
     * @param balance Initial balance (must be >= 0)
     * @param accountType Type of account
     * @param status Account status
     * @param pin 4-digit security PIN
     * @throws IllegalArgumentException if age < 18, balance < 0, or PIN is invalid
     */
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
    
    /**
     * Withdraw money with PIN verification and status check.
     * 
     * @param amount Amount to withdraw
     * @param enteredPin PIN for authentication
     * @return true if withdrawal successful, false otherwise
     */
    public boolean withdraw(double amount, String enteredPin) {
        // Verify PIN
        if (!validatePin(enteredPin)) {
            return false;
        }
        
        // Check account status
        if (!status.equals("ACTIVE")) {
            return false;
        }
        
        // Check amount validity and balance
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Validate if entered PIN matches the stored PIN.
     * 
     * @param enteredPin The PIN to validate
     * @return true if PIN matches, false otherwise or if input is empty
     */
    public boolean validatePin(String enteredPin) {
        if (enteredPin == null || enteredPin.isEmpty()) {
            return false;
        }
        return this.pin.equals(enteredPin);
    }
    
    /**
     * Change the account PIN after validating the old PIN.
     * 
     * @param oldPin The current PIN
     * @param newPin The new PIN to set
     * @return true if PIN changed successfully, false otherwise
     */
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
    
    /**
     * Helper method to validate PIN format (exactly 4 digits).
     * 
     * @param pin The PIN to validate
     * @return true if PIN is valid, false otherwise
     */
    private boolean isValidPin(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        // Check if all characters are digits
        return pin.matches("\\d{4}");
    }
    
    /**
     * Suspend the account.
     */
    public void suspend() {
        this.status = "SUSPENDED";
    }
    
    /**
     * Activate the account.
     */
    public void activate() {
        this.status = "ACTIVE";
    }
    
    /**
     * Close the account.
     */
    public void close() {
        this.status = "CLOSED";
    }
    
    /**
     * Display account information to the console.
     */
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
