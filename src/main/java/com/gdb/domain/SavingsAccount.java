package com.gdb.domain;

import com.gdb.exceptions.*;

/**
 * SavingsAccount class extends Account with interest calculation and minimum balance requirements.
 */
public class SavingsAccount extends Account {
    
    private double minBalance;
    private double interestRate;
    
    /**
     * Constructor for SavingsAccount.
     * 
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age
     * @param balance Initial balance
     * @param status Account status
     * @param pin 4-digit security PIN
     * @param minBalance Minimum balance required
     * @param interestRate Interest rate percentage per annum
     */
    public SavingsAccount(String accountNumber, String name, int age, double balance, 
                         String status, String pin, double minBalance, double interestRate) {
        super(accountNumber, name, age, balance, "SAVINGS", status, pin);
        this.minBalance = minBalance;
        this.interestRate = interestRate;
    }
    
    /**
     * Apply interest to the account balance.
     */
    public void applyInterest() {
        double interest = getBalance() * (interestRate / 100);
        try {
            deposit(interest);
        } catch (InvalidAmountException e) {
            System.out.println("Error applying interest: " + e.getMessage());
        }
    }
    
    /**
     * Override withdraw method to enforce minimum balance requirement.
     * 
     * @param amount Amount to withdraw
     * @param enteredPin PIN for authentication
     * @return true if withdrawal successful
     * @throws InvalidPinException if PIN is incorrect
     * @throws InactiveAccountException if account is not active
     * @throws InvalidAmountException if amount is invalid
     * @throws InsufficientBalanceException if insufficient balance
     * @throws MinimumBalanceViolationException if withdrawal would breach minimum balance
     */
    @Override
    public boolean withdraw(double amount, String enteredPin) throws InvalidPinException,
                                                                     InactiveAccountException,
                                                                     InvalidAmountException,
                                                                     InsufficientBalanceException,
                                                                     MinimumBalanceViolationException,
                                                                     AccountException {
        // Check if withdrawal would violate minimum balance
        if ((getBalance() - amount) < minBalance) {
            throw new MinimumBalanceViolationException("Withdrawal would breach minimum balance of Rs " + minBalance);
        }
        
        // Call parent class withdraw method
        return super.withdraw(amount, enteredPin);
    }
    
    /**
     * Get the minimum balance.
     * @return Minimum balance required
     */
    public double getMinBalance() {
        return minBalance;
    }
    
    /**
     * Get the interest rate.
     * @return Interest rate percentage
     */
    public double getInterestRate() {
        return interestRate;
    }
    
    /**
     * Set the minimum balance.
     * @param minBalance New minimum balance
     */
    public void setMinBalance(double minBalance) {
        this.minBalance = minBalance;
    }
    
    /**
     * Set the interest rate.
     * @param interestRate New interest rate percentage
     */
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}
