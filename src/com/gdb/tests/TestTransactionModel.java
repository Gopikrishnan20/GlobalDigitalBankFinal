package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.service.TransferService;
import com.gdb.exceptions.*;

public class TestTransactionModel {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 16 — TRANSACTION MODEL TEST");
        System.out.println("=".repeat(60));

        SavingsAccount acc1 = testDepositWithTransaction();
        testWithdrawWithTransaction(acc1);
        testTransferWithTransaction(acc1, new TransferService());
        testLegacyDeposit(acc1);
    }

    private static SavingsAccount testDepositWithTransaction() throws Exception {
        SavingsAccount account = new SavingsAccount(1001, "Rajesh Sharma", 30, 50000);
        account.setPin(1234);
        Transaction transaction = account.depositWithTransaction(5000);
        System.out.println("[STEP 10] Deposit Transaction: " + transaction);
        return account;
    }

    private static void testWithdrawWithTransaction(SavingsAccount account) throws Exception {
        Transaction transaction = account.withdrawWithTransaction(2000, 1234);
        System.out.println("[STEP 11] Withdrawal Transaction: " + transaction);
    }

    private static void testTransferWithTransaction(SavingsAccount from, TransferService service)
            throws Exception {
        SavingsAccount to = new SavingsAccount(1002, "Priya Patel", 28, 20000);
        Transaction transaction = service.transferWithTransaction(from, to, 1000, 1234);
        System.out.println("[STEP 12] Transfer Transaction: " + transaction);
    }

    private static void testLegacyDeposit(SavingsAccount account) throws Exception {
        account.deposit(1000);
        System.out.println("[STEP 13] Legacy Deposit +1000: " + account.getAccountInfo());
    }
}
