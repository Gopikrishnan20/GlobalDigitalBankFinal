package com.gdb.tests;

import com.gdb.domain.Account;

/**
 * Test class for Account functionality.
 * Tests basic account operations: deposit and withdrawal.
 */
public class TestAccount {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 1: Basic Account Test ===");
        
        // Create a new account
        Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000, "SAVINGS", "ACTIVE");
        
        // Display account information
        account.displayAccountInfo();
        
        // Test deposit
        boolean depositResult = account.deposit(2000);
        System.out.println("Deposit 2000: " + (depositResult ? "SUCCESS" : "FAILED") + 
                         " | Balance: Rs " + account.getBalance());
        
        // Test withdrawal (valid)
        boolean withdrawResult1 = account.withdraw(3000);
        System.out.println("Withdraw 3000: " + (withdrawResult1 ? "SUCCESS" : "FAILED") + 
                         " | Balance: Rs " + account.getBalance());
        
        // Test withdrawal (exceeds balance)
        boolean withdrawResult2 = account.withdraw(10000);
        System.out.println("Withdraw 10000 (exceeds balance): " + (withdrawResult2 ? "SUCCESS" : "FAILED") + 
                         " | Balance: Rs " + account.getBalance());
    }
}
