package com.gdb.domain;

import com.gdb.exceptions.*;

/**
 * CurrentAccount class extends Account with overdraft facility.
 */
public class CurrentAccount extends Account {
    
    private double overdraftLimit;
    
    /**
     * Constructor for CurrentAccount.
     * 
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age
     * @param balance Initial balance
     * @param status Account status
     * @param pin 4-digit security PIN
     * @param overdraftLimit Maximum overdraft allowed
     */
    public CurrentAccount(String accountNumber, String name, int age, double balance, 
                         String status, String pin, double overdraftLimit) {
        super(accountNumber, name, age, balance, "CURRENT", status, pin);
        this.overdraftLimit = overdraftLimit;
    }
    
    /**
     * Override withdraw method to allow overdraft facility.
     * Balance can go negative up to the overdraft limit.
     * 
     * @param amount Amount to withdraw
     * @param enteredPin PIN for authentication
     * @return true if withdrawal successful
     * @throws InvalidPinException if PIN is incorrect
     * @throws InactiveAccountException if account is not active
     * @throws InvalidAmountException if amount is invalid
     * @throws InsufficientBalanceException if amount exceeds balance + overdraft limit
     */
    @Override
    public boolean withdraw(double amount, String enteredPin) throws InvalidPinException,
                                                                     InactiveAccountException,
                                                                     InvalidAmountException,
                                                                     InsufficientBalanceException,
                                                                     MinimumBalanceViolationException,
                                                                     AccountException {
        // Verify PIN
        if (!validatePin(enteredPin)) {
            throw new InvalidPinException("Invalid PIN entered");
        }
        
        // Check account status
        if (!getStatus().equals("ACTIVE")) {
            if (getStatus().equals("CLOSED")) {
                throw new InactiveAccountException("Account is closed");
            } else {
                throw new InactiveAccountException("Account is not active");
            }
        }
        
        // Check amount validity
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        
        // Check if amount exceeds available funds (balance + overdraft limit)
        double availableFunds = getBalance() + overdraftLimit;
        if (amount > availableFunds) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        
        // Perform withdrawal (allowing balance to go negative up to -overdraftLimit)
        setBalance(getBalance() - amount);
        return true;
    }
    
    /**
     * Get the overdraft limit.
     * @return Overdraft limit amount
     */
    public double getOverdraftLimit() {
        return overdraftLimit;
    }
    
    /**
     * Set the overdraft limit.
     * @param overdraftLimit New overdraft limit
     */
    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
}
