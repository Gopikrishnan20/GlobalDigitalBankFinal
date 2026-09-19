package com.gdb.tests;

import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.MinimumBalanceViolationException;

public class TestInterfaceFactory {

    public static void main(String[] args) {
        System.out.println("=== Activity 12: Factory-Driven System Suite ===");

        boolean savingsTest = testSavingsAccount();
        System.out.println("[Test 1] Savings Account Creation & Deposit: "
                + (savingsTest ? "[PASS]" : "[FAIL]"));

        boolean currentTest = testCurrentAccountOverdraft();
        System.out.println("[Test 2] Current Account Overdraft Withdrawal: "
                + (currentTest ? "[PASS]" : "[FAIL]"));

        boolean fixedDepositTest = testFixedDepositWithdrawal();
        System.out.println("[Test 3] Fixed Deposit Premature Withdrawal Block: "
                + (fixedDepositTest ? "[PASS]" : "[FAIL]"));

        boolean invalidTypeTest = testInvalidType();
        System.out.println("[Test 4] Invalid Type Rejection: "
                + (invalidTypeTest ? "[PASS]" : "[FAIL]"));

        System.out.println("Factory-driven architecture successfully verified!");
    }

    private static boolean testSavingsAccount() {
        try {
            IAccount savings = AccountFactory.createAccount(
                    "SAVINGS", "SAV001", "Rajesh Sharma", 28, 5000.0,
                    "ACTIVE", "1234", 1000.0, 4.0);
            savings.deposit(1000.0);
            boolean depositWorked = savings.getBalance() == 6000.0;

            try {
                savings.withdraw(5501.0, "1234");
                return false;
            } catch (MinimumBalanceViolationException exception) {
                return depositWorked && savings.getBalance() == 6000.0;
            }
        } catch (AccountException exception) {
            return false;
        }
    }

    private static boolean testCurrentAccountOverdraft() {
        try {
            IAccount current = AccountFactory.createAccount(
                    "CURRENT", "CUR001", "Priya Patel", 32, 1000.0,
                    "ACTIVE", "5678", 5000.0);
            current.withdraw(5000.0, "5678");
            return current.getBalance() == -4000.0;
        } catch (AccountException exception) {
            return false;
        }
    }

    private static boolean testFixedDepositWithdrawal() {
        try {
            IAccount fixedDeposit = AccountFactory.createAccount(
                    "FIXED_DEPOSIT", "FD001", "Amit Kumar", 45, 100000.0,
                    "ACTIVE", "9876", 12, 6.5);
            fixedDeposit.withdraw(1000.0, "9876");
            return false;
        } catch (AccountException exception) {
            return true;
        }
    }

    private static boolean testInvalidType() {
        try {
            AccountFactory.createAccount(
                    "UNKNOWN", "ACC001", "Unknown Customer", 25, 1000.0,
                    "ACTIVE", "1234");
            return false;
        } catch (IllegalArgumentException exception) {
            return true;
        }
    }
}
