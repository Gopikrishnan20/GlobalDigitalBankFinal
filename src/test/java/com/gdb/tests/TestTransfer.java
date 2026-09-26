package com.gdb.tests;

import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InsufficientBalanceException;
import com.gdb.service.TransferService;

public class TestTransfer {

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println("  ACTIVITY 15 - TRANSFER WITH DAILY LIMITS");
        System.out.println("============================================================");

        Account acc1 = (Account) AccountFactory.createAccount("SAVINGS", "1001", "Rajesh Sharma",
                30, 100000.0, "ACTIVE", "1234", 0);
        Account acc2 = (Account) AccountFactory.createAccount("SAVINGS", "1002", "Priya Patel",
                28, 20000.0, "ACTIVE", "1234", 0);
        TransferService transferService = new TransferService();

        System.out.printf("[STEP 9] Account #%s | %s (%d yrs, Tenure: %d yrs) | Savings | Rs. %.1f | Active%n",
                acc1.getAccountNumber(), acc1.getName(), acc1.getAge(), acc1.getTenureYears(), acc1.getBalance());
        System.out.printf("[STEP 9] Account #%s | %s (%d yrs, Tenure: %d yrs) | Savings | Rs. %.1f | Active%n",
                acc2.getAccountNumber(), acc2.getName(), acc2.getAge(), acc2.getTenureYears(), acc2.getBalance());

        transferService.transfer(acc1, acc2, 5000.0, "1234");
        System.out.printf("[STEP 10] Transfer Rs. 5,000: SUCCESS | acc1 = Rs. %.1f | acc2 = Rs. %.1f%n",
                acc1.getBalance(), acc2.getBalance());

        double senderBalanceBeforeOversizedTransfer = acc1.getBalance();
        double recipientBalanceBeforeOversizedTransfer = acc2.getBalance();
        try {
            transferService.transfer(acc1, acc2, 100000.0, "1234");
            throw new AssertionError("Expected insufficient-balance rejection");
        } catch (InsufficientBalanceException exception) {
            System.out.println("[STEP 11] Caught InsufficientBalanceException: " + exception.getMessage());
            if (acc1.getBalance() != senderBalanceBeforeOversizedTransfer
                    || acc2.getBalance() != recipientBalanceBeforeOversizedTransfer) {
                throw new AssertionError("Rejected transfer changed an account balance");
            }
        }

        System.out.printf("[STEP 12] Daily limit for acc1: Rs. %.1f%n", acc1.getDailyTransferLimit());
        int transferNumber = 1;
        while (true) {
            double senderBalanceBeforeAttempt = acc1.getBalance();
            double recipientBalanceBeforeAttempt = acc2.getBalance();
            try {
                transferService.transfer(acc1, acc2, 20000.0, "1234");
                System.out.printf("  Transfer #%d of Rs. 20,000: SUCCESS | used today = Rs. %.1f%n",
                        transferNumber++, acc1.getDailyTransferTotal());
            } catch (AccountException exception) {
                System.out.println("[STEP 12] Caught AccountException: " + exception.getMessage());
                if (acc1.getBalance() != senderBalanceBeforeAttempt
                        || acc2.getBalance() != recipientBalanceBeforeAttempt) {
                    throw new AssertionError("Daily-limit rejection changed an account balance");
                }
                break;
            }
        }

        System.out.printf("[STEP 13] Used today: Rs. %.1f | Remaining: Rs. %.1f%n",
                acc1.getDailyTransferTotal(), acc1.getRemainingDailyTransferLimit());
        if (acc1.getDailyTransferTotal() + acc1.getRemainingDailyTransferLimit()
                != acc1.getDailyTransferLimit()) {
            throw new AssertionError("Used and remaining transfer limit do not reconcile");
        }

        Account fixedDeposit = (Account) AccountFactory.createAccount("FIXED_DEPOSIT", "FD1001",
                "Fixed Deposit Customer", 40, 100000.0, "ACTIVE", "4321", 12, 6.5);
        double fixedDepositBalanceBeforeTransfer = fixedDeposit.getBalance();
        try {
            transferService.transfer(fixedDeposit, acc2, 1.0, "4321");
            throw new AssertionError("Expected zero-limit fixed-deposit transfer rejection");
        } catch (AccountException exception) {
            if (!exception.getMessage().startsWith("Daily transfer limit exceeded")) {
                throw exception;
            }
            if (fixedDeposit.getBalance() != fixedDepositBalanceBeforeTransfer
                    || acc2.getBalance() != recipientBalanceBeforeOversizedTransfer + 40000.0) {
                throw new AssertionError("Rejected fixed-deposit transfer changed an account balance");
            }
        }
    }
}