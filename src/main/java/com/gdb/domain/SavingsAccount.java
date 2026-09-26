package com.gdb.domain;

import com.gdb.exceptions.*;
public class SavingsAccount extends Account {
    
    private int tenureYears;
    private double minBalance;
    private double interestRate;
    
    /**
     * Constructor for SavingsAccount.
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
        this.tenureYears = 0;
        this.minBalance = minBalance;
        this.interestRate = interestRate;
    }

    /**
     * Constructor for a savings account whose terms are based on customer tenure.
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age
     * @param balance Initial balance
     * @param status Account status
     * @param pin 4-digit security PIN
     * @param tenureYears Customer tenure in years
     */
    public SavingsAccount(String accountNumber, String name, int age, double balance,
                         String status, String pin, int tenureYears) {
        super(accountNumber, name, age, balance, "SAVINGS", status, pin);
        this.tenureYears = tenureYears;
        this.minBalance = AccountRulesEngine.getSavingsMinBalance(tenureYears);
        this.interestRate = AccountRulesEngine.getSavingsInterestRate(tenureYears);
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
    
    @Override
    public void processDebit(double amount) throws AccountException {
        if (getBalance() - amount < minBalance) {
            throw new MinimumBalanceViolationException("Withdrawal would breach minimum balance of Rs " + minBalance);
        }
        super.processDebit(amount);
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