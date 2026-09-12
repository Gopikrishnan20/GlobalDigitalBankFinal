package com.gdb.tests;

import com.gdb.domain.Account;
import com.gdb.exceptions.*;

public class TestAccountExceptions {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 5: Custom Exceptions Test ===");
        
        // Create a valid account with initial balance
        Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000.0, "SAVINGS", "ACTIVE", "1234");
        
        // Test 1: InvalidPinException
        try {
            account.withdraw(1000.0, "9999");
        } catch (InvalidPinException e) {
            System.out.println("Caught InvalidPinException as expected: " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("[FAIL] Unexpected exception: " + e.getMessage());
        }
        
        // Test 2: InactiveAccountException
        try {
            account.suspend();
            account.withdraw(1000.0, "1234");
        } catch (InactiveAccountException e) {
            System.out.println("Caught InactiveAccountException as expected: " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("[FAIL] Unexpected exception: " + e.getMessage());
        }
        
        // Reactivate for next tests
        account.activate();
        
        // Test 3: InvalidAmountException
        try {
            account.withdraw(0, "1234");
        } catch (InvalidAmountException e) {
            System.out.println("Caught InvalidAmountException as expected: " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("[FAIL] Unexpected exception: " + e.getMessage());
        }
        
        // Test 4: InsufficientBalanceException
        try {
            account.withdraw(10000.0, "1234");
        } catch (InsufficientBalanceException e) {
            System.out.println("Caught InsufficientBalanceException as expected: " + e.getMessage());
        } catch (AccountException e) {
            System.out.println("[FAIL] Unexpected exception: " + e.getMessage());
        }
        
        // Test 5: Successful withdrawal
        try {
            account.withdraw(1000.0, "1234");
            System.out.println("Successful withdrawal completed: Rs 1000.0 | New Balance: Rs " + account.getBalance());
        } catch (AccountException e) {
            System.out.println("[FAIL] Unexpected exception during withdrawal: " + e.getMessage());
        }
    }
}
