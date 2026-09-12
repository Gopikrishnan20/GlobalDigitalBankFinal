package com.gdb.tests;

import com.gdb.domain.*;

/**
 * Test class for Account subclasses.
 * Tests the creation and functionality of SavingsAccount, CurrentAccount, 
 * FixedDepositAccount, and SalaryAccount.
 */
public class TestAccountSubclasses {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 7: Account Subclasses Test ===");
        
        // Test 1: Create SavingsAccount
        SavingsAccount savingsAccount = new SavingsAccount(
            "SAV001", 
            "Raj Kumar", 
            28, 
            10000.0, 
            "ACTIVE", 
            "1234", 
            1000.0,  // minBalance
            4.0      // interestRate
        );
        System.out.println("Savings Account Created: Balance Rs " + savingsAccount.getBalance() + 
                         " | Min Balance: Rs " + savingsAccount.getMinBalance());
        
        // Test 2: Create CurrentAccount
        CurrentAccount currentAccount = new CurrentAccount(
            "CUR001", 
            "Priya Singh", 
            32, 
            50000.0, 
            "ACTIVE", 
            "5678", 
            25000.0  // overdraftLimit
        );
        System.out.println("Current Account Created: Overdraft Limit Rs " + currentAccount.getOverdraftLimit());
        
        // Test 3: Create FixedDepositAccount
        FixedDepositAccount fdAccount = new FixedDepositAccount(
            "FD001", 
            "Amit Patel", 
            45, 
            100000.0, 
            "ACTIVE", 
            "9876", 
            12,      // tenureMonths
            6.5      // interestRate
        );
        System.out.println("Fixed Deposit Created: Tenure " + fdAccount.getTenureMonths() + 
                         " months | Interest: " + fdAccount.getInterestRate() + "%");
        
        // Test 4: Create SalaryAccount
        SalaryAccount salaryAccount = new SalaryAccount(
            "SAL001", 
            "Neha Verma", 
            26, 
            25000.0, 
            "ACTIVE", 
            "4321", 
            "Infosys",  // employerName
            0          // inactiveMonths
        );
        System.out.println("Salary Account Created: Employer " + salaryAccount.getEmployerName());
        
        System.out.println("All subclasses instantiated successfully!");
    }
}
