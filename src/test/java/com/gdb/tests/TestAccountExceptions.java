package com.gdb.tests;

import com.gdb.domain.Account;
import com.gdb.exceptions.*;

public class TestAccountExceptions {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 6: Exception Handling Suite ===");
        
        // Create a valid account for testing
        Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000.0, "SAVINGS", "ACTIVE", "1234");
        
        // Test 1: Handle Invalid PIN Exception
        try {
            account.withdraw(1000.0, "9999");
            System.out.println("[Test 1] Failed to catch InvalidPinException [FAIL]");
        } catch (InvalidPinException e) {
            System.out.println("[Test 1] Caught Invalid PIN: " + e.getMessage() + " [PASS]");
        } catch (AccountException e) {
            System.out.println("[Test 1] Unexpected exception: " + e.getMessage() + " [FAIL]");
        }
        
        // Test 2: Handle Inactive Account Exception
        try {
            account.suspend();
            account.withdraw(1000.0, "1234");
            System.out.println("[Test 2] Failed to catch InactiveAccountException [FAIL]");
        } catch (InactiveAccountException e) {
            System.out.println("[Test 2] Caught Inactive Account: " + e.getMessage() + " [PASS]");
        } catch (AccountException e) {
            System.out.println("[Test 2] Unexpected exception: " + e.getMessage() + " [FAIL]");
        }
        
        // Reactivate for next tests
        account.activate();
        
        // Test 3: Handle Invalid Amount Exception (Deposit)
        try {
            account.deposit(-500.0);
            System.out.println("[Test 3] Failed to catch InvalidAmountException [FAIL]");
        } catch (InvalidAmountException e) {
            System.out.println("[Test 3] Caught Invalid Amount: " + e.getMessage() + " [PASS]");
        } catch (AccountException e) {
            System.out.println("[Test 3] Unexpected exception: " + e.getMessage() + " [FAIL]");
        }
        
        // Test 4: Handle Insufficient Balance Exception
        try {
            account.withdraw(10000.0, "1234");
            System.out.println("[Test 4] Failed to catch InsufficientBalanceException [FAIL]");
        } catch (InsufficientBalanceException e) {
            System.out.println("[Test 4] Caught Insufficient Funds: " + e.getMessage() + " [PASS]");
        } catch (AccountException e) {
            System.out.println("[Test 4] Unexpected exception: " + e.getMessage() + " [FAIL]");
        }
        
        // Test 5: Polymorphic Catch with Base Exception (Closed Account)
        try {
            account.close();
            account.withdraw(100.0, "1234");
            System.out.println("[Test 5] Failed to catch AccountException [FAIL]");
        } catch (AccountException e) {
            // Polymorphic catch - catches any AccountException subclass
            System.out.println("[Test 5] Polymorphic Handler caught: " + e.getMessage() + " [PASS]");
        }
        
        System.out.println("All exception handling tests completed successfully!");
    }
}

