package com.gdb.domain;

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
