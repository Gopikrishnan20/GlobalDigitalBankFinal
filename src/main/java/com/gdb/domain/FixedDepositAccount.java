package com.gdb.domain;

/**
 * FixedDepositAccount class extends Account with fixed deposit and compound interest calculation.
 */
public class FixedDepositAccount extends Account {
    
    private int tenureMonths;
    private double interestRate;
    
    /**
     * Constructor for FixedDepositAccount.
     * 
     * @param accountNumber Unique account identifier
     * @param name Account holder's name
     * @param age Account holder's age
     * @param balance Initial investment amount
     * @param status Account status
     * @param pin 4-digit security PIN
     * @param tenureMonths Duration of fixed deposit in months
     * @param interestRate Interest rate percentage per annum
     */
    public FixedDepositAccount(String accountNumber, String name, int age, double balance, 
                              String status, String pin, int tenureMonths, double interestRate) {
        super(accountNumber, name, age, balance, "FIXED_DEPOSIT", status, pin);
        this.tenureMonths = tenureMonths;
        this.interestRate = interestRate;
    }
    
    /**
     * Calculate the maturity amount using compound interest formula.
     * Formula: A = P * (1 + r/100)^(t/12)
     * where t is tenure in months
     * 
     * @return Maturity amount after interest
     */
    public double calculateMaturityAmount() {
        double rate = interestRate / 100.0;
        double years = tenureMonths / 12.0;
        double maturityAmount = getBalance() * Math.pow(1 + rate, years);
        return maturityAmount;
    }
    
    /**
     * Get the tenure in months.
     * @return Tenure in months
     */
    public int getTenureMonths() {
        return tenureMonths;
    }
    
    /**
     * Get the interest rate.
     * @return Interest rate percentage
     */
    public double getInterestRate() {
        return interestRate;
    }
    
    /**
     * Set the tenure in months.
     * @param tenureMonths New tenure in months
     */
    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }
    
    /**
     * Set the interest rate.
     * @param interestRate New interest rate percentage
     */
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
}
