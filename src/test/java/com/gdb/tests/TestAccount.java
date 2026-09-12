package com.gdb.tests;

import com.gdb.domain.Account;

public class TestAccount {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 3: Enhanced Account Test ===");
        
        try {
            // Create a new account with PIN
            Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000.0, "SAVINGS", "ACTIVE", "1234");
            
            // Display initial status
            System.out.println("Initial Balance: Rs " + account.getBalance() + " | Status: " + account.getStatus());
            
            // Test 1: Withdraw with correct PIN
            boolean withdrawCorrect = account.withdraw(1000.0, "1234");
            System.out.println("Withdraw with correct PIN: " + (withdrawCorrect ? "SUCCESS" : "FAILED") + " | Balance: Rs " + account.getBalance());
            
            // Test 2: Withdraw with wrong PIN
            boolean withdrawWrong = account.withdraw(500.0, "5678");
            System.out.println("Withdraw with wrong PIN: " + (withdrawWrong ? "SUCCESS" : "FAILED") + " | Balance: Rs " + account.getBalance());
            
            // Test 3: Suspend account and try to withdraw
            System.out.println("Account Suspended.");
            account.suspend();
            boolean withdrawSuspended = account.withdraw(500.0, "1234");
            System.out.println("Withdraw on SUSPENDED account: " + (withdrawSuspended ? "SUCCESS" : "FAILED") + " | Balance: Rs " + account.getBalance());
            
            // Test 4: Reactivate account
            System.out.println("Account Re-Activated.");
            account.activate();
            
            // Test 5: Change PIN
            boolean pinChanged = account.changePin("1234", "5678");
            System.out.println("PIN Changed " + (pinChanged ? "Successfully." : "Failed."));
            
            // Test 6: Withdraw with new PIN
            boolean withdrawNewPin = account.withdraw(1000.0, "5678");
            System.out.println("Withdraw with new PIN: " + (withdrawNewPin ? "SUCCESS" : "FAILED") + " | Balance: Rs " + account.getBalance());
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
