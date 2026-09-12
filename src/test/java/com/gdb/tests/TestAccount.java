package com.gdb.tests;

import com.gdb.domain.Account;

public class TestAccount {
    
    public static void main(String[] args) {
        System.out.println("=== Activity 2: Test Account Suite ===");
        
        // Create a new account with initial balance of 5000.0
        Account account = new Account("ACC1001", "Rajesh Sharma", 28, 5000.0, "SAVINGS", "ACTIVE");
        
        // Test 1: Check initial balance
        boolean test1 = (account.getBalance() == 5000.0);
        System.out.println("Test 1 (Initial Balance 5000.0): " + (test1 ? "[PASS]" : "[FAIL]"));
        
        // Test 2: Test valid deposit
        boolean depositResult = account.deposit(2000.0);
        boolean test2 = (depositResult == true && account.getBalance() == 7000.0);
        System.out.println("Test 2 (Deposit 2000.0 -> Balance 7000.0): " + (test2 ? "[PASS]" : "[FAIL]"));
        
        // Test 3: Test negative deposit (edge case)
        boolean negDepositResult = account.deposit(-500.0);
        boolean test3 = (negDepositResult == false && account.getBalance() == 7000.0);
        System.out.println("Test 3 (Negative Deposit -> Rejected): " + (test3 ? "[PASS]" : "[FAIL]"));
        
        // Test 4: Test valid withdrawal
        boolean withdrawResult = account.withdraw(3000.0);
        boolean test4 = (withdrawResult == true && account.getBalance() == 4000.0);
        System.out.println("Test 4 (Withdraw 3000.0 -> Balance 4000.0): " + (test4 ? "[PASS]" : "[FAIL]"));
        
        // Test 5: Test withdrawal exceeding balance (edge case)
        boolean exceedWithdrawResult = account.withdraw(10000.0);
        boolean test5 = (exceedWithdrawResult == false && account.getBalance() == 4000.0);
        System.out.println("Test 5 (Exceeding Withdrawal -> Rejected): " + (test5 ? "[PASS]" : "[FAIL]"));
        
        // Test 6: Test negative withdrawal (edge case)
        boolean negWithdrawResult = account.withdraw(-100.0);
        boolean test6 = (negWithdrawResult == false && account.getBalance() == 4000.0);
        System.out.println("Test 6 (Negative Withdrawal -> Rejected): " + (test6 ? "[PASS]" : "[FAIL]"));
        
        System.out.println("All Account tests completed successfully!");
    }
}
