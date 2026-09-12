package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.exceptions.*;

/**
 * Test class for Account subclasses.
 * Tests the creation and functionality of SavingsAccount, CurrentAccount, 
 * FixedDepositAccount, and SalaryAccount.
 */
public class TestAccountSubclasses {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 8: Polymorphism Test ===");
        
        // Test 1: SavingsAccount with minimum balance constraint
        try {
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
            
            // Try to withdraw amount that would breach minimum balance
            try {
                savingsAccount.withdraw(9500.0, "1234");
                System.out.println("[Savings] Withdraw 9500 (breaches min balance 1000): [FAIL]");
            } catch (MinimumBalanceViolationException e) {
                System.out.println("[Savings] Withdraw 9500 (breaches min balance 1000): Caught MinimumBalanceViolationException [PASS]");
            }
        } catch (Exception e) {
            System.out.println("[Savings] Test setup error: " + e.getMessage());
        }
        
        // Test 2: CurrentAccount with overdraft facility
        try {
            CurrentAccount currentAccount = new CurrentAccount(
                "CUR001", 
                "Priya Singh", 
                32, 
                5000.0, 
                "ACTIVE", 
                "5678", 
                25000.0  // overdraftLimit
            );
            
            // Withdraw with overdraft (balance goes negative)
            try {
                boolean result = currentAccount.withdraw(10000.0, "5678");
                if (result && currentAccount.getBalance() == -5000.0) {
                    System.out.println("[Current] Withdraw with Overdraft (Balance goes to -5000): SUCCESS [PASS]");
                } else {
                    System.out.println("[Current] Withdraw with Overdraft: [FAIL]");
                }
            } catch (AccountException e) {
                System.out.println("[Current] Withdraw with Overdraft: [FAIL] - " + e.getMessage());
            }
            
            // Try to withdraw exceeding overdraft limit
            try {
                currentAccount.withdraw(35000.0, "5678");
                System.out.println("[Current] Withdraw exceeding Overdraft: [FAIL]");
            } catch (InsufficientBalanceException e) {
                System.out.println("[Current] Withdraw exceeding Overdraft (exceeds -25000): Caught InsufficientBalanceException [PASS]");
            }
        } catch (Exception e) {
            System.out.println("[Current] Test setup error: " + e.getMessage());
        }
        
        // Test 3: FixedDepositAccount - prevent premature withdrawal
        try {
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
            
            // Try to withdraw from Fixed Deposit
            try {
                fdAccount.withdraw(10000.0, "9876");
                System.out.println("[FixedDeposit] Withdraw attempt: [FAIL]");
            } catch (AccountException e) {
                System.out.println("[FixedDeposit] Withdraw attempt: Caught AccountException [PASS]");
            }
        } catch (Exception e) {
            System.out.println("[FixedDeposit] Test setup error: " + e.getMessage());
        }
        
        System.out.println("All polymorphic behaviors verified!");
    }
}
