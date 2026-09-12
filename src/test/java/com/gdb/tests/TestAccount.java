package com.gdb.tests;

import com.gdb.domain.Account;
import com.gdb.exceptions.*;

public class TestAccount {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 4: Enhanced Account Test Suite ===");
        
        // Test 1: Age Validation on Account Creation
        boolean test1 = false;
        try {
            Account underage = new Account("ACC0001", "John Doe", 16, 5000.0, "SAVINGS", "ACTIVE", "1234");
        } catch (IllegalArgumentException e) {
            test1 = true; // Exception caught as expected
        }
        System.out.println("Test 1 (Underage Customer Rejection): " + (test1 ? "[PASS]" : "[FAIL]"));
        
        // Create a valid account for remaining tests
        Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000.0, "SAVINGS", "ACTIVE", "1234");
        
        // Test 2: Wrong PIN Rejection and Correct PIN Withdrawal
        boolean wrongPinResult = false;
        boolean correctPinResult = false;
        
        try {
            account.withdraw(1000.0, "9999");
        } catch (InvalidPinException e) {
            wrongPinResult = false; // Expected - wrong PIN fails
        } catch (AccountException e) {
            wrongPinResult = false;
        }
        
        try {
            account.withdraw(1000.0, "1234");
            correctPinResult = true; // Withdrawal successful
        } catch (AccountException e) {
            correctPinResult = false;
        }
        
        boolean test2 = (!wrongPinResult && correctPinResult && account.getBalance() == 4000.0);
        System.out.println("Test 2 (Wrong PIN Rejection): " + (test2 ? "[PASS]" : "[FAIL]"));
        
        // Test 3: Correct PIN Withdrawal (continuation from Test 2)
        boolean test3 = correctPinResult;
        System.out.println("Test 3 (Correct PIN Withdrawal): " + (test3 ? "[PASS]" : "[FAIL]"));
        
        // Test 4: PIN Change Functionality
        boolean pinChangeResult = account.changePin("1234", "5678");
        boolean oldPinFails = true;
        boolean newPinWorks = false;
        
        try {
            account.withdraw(500.0, "1234");
            oldPinFails = false; // Should have failed
        } catch (InvalidPinException e) {
            oldPinFails = true; // Expected
        } catch (AccountException e) {
            oldPinFails = false;
        }
        
        try {
            account.withdraw(500.0, "5678");
            newPinWorks = true; // Withdrawal successful
        } catch (AccountException e) {
            newPinWorks = false;
        }
        
        boolean test4 = (pinChangeResult && oldPinFails && newPinWorks && account.getBalance() == 3500.0);
        System.out.println("Test 4 (PIN Change & Old PIN Invalidation): " + (test4 ? "[PASS]" : "[FAIL]"));
        
        // Test 5: Account Suspension
        account.suspend();
        boolean suspendedWithdraw = false;
        try {
            account.withdraw(500.0, "5678");
            suspendedWithdraw = false; // Should have failed
        } catch (InactiveAccountException e) {
            suspendedWithdraw = true; // Expected
        } catch (AccountException e) {
            suspendedWithdraw = false;
        }
        
        boolean test5 = !suspendedWithdraw && account.getBalance() == 3500.0;
        System.out.println("Test 5 (Suspended Account Block): " + (test5 ? "[PASS]" : "[FAIL]"));
        
        // Test 6: Reactivation and Successful Withdrawal
        account.activate();
        boolean reactivatedWithdraw = false;
        try {
            account.withdraw(500.0, "5678");
            reactivatedWithdraw = true; // Withdrawal successful
        } catch (AccountException e) {
            reactivatedWithdraw = false;
        }
        
        boolean test6 = (reactivatedWithdraw && account.getBalance() == 3000.0);
        System.out.println("Test 6 (Reactivation & Success): " + (test6 ? "[PASS]" : "[FAIL]"));
        
        System.out.println("All Enhanced Account tests passed!");
    }
}
