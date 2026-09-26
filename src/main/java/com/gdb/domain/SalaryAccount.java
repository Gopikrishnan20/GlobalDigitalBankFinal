package com.gdb.domain;

import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InsufficientBalanceException;

/**
 * SalaryAccount class extends Account for salary-based banking operations.
 */
public class SalaryAccount extends Account {
    
    private String employerName;
    private int inactiveMonths;
    
    /**
     * Constructor for SalaryAccount.
     * 
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age
     * @param balance Initial balance
     * @param status Account status
     * @param pin 4-digit security PIN
     * @param employerName Name of the employer
     * @param inactiveMonths Number of months the account has been inactive
     */
    public SalaryAccount(String accountNumber, String name, int age, double balance, 
                        String status, String pin, String employerName, int inactiveMonths) {
        super(accountNumber, name, age, balance, "SALARY", status, pin);
        this.employerName = employerName;
        this.inactiveMonths = inactiveMonths;
    }

    @Override
    public void processDebit(double amount) throws AccountException {
        if (amount > getBalance()) {
            throw new InsufficientBalanceException("Insufficient funds in account");
        }
        setBalance(getBalance() - amount);
    }

    public boolean hasSalaryCreditHistory() {
        return AccountRulesEngine.hasSalaryCreditHistory(inactiveMonths);
    }
    
    /**
     * Get the employer name.
     * @return Employer name
     */
    public String getEmployerName() {
        return employerName;
    }
    
    /**
     * Get the number of inactive months.
     * @return Inactive months
     */
    public int getInactiveMonths() {
        return inactiveMonths;
    }
    
    /**
     * Set the employer name.
     * @param employerName New employer name
     */
    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }
    
    /**
     * Set the number of inactive months.
     * @param inactiveMonths New inactive months value
     */
    public void setInactiveMonths(int inactiveMonths) {
        this.inactiveMonths = inactiveMonths;
    }
}
