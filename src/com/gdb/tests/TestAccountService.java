package com.gdb.tests;

import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import java.util.List;

public class TestAccountService {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 19 — ACCOUNT SERVICE DEMO");
        System.out.println("=".repeat(60));

        // STEP 12: Wire dependencies
        TransactionLogger logger = new TransactionLogger(new MemoryLogDestination());
        AccountService service = new AccountService(logger);

        System.out.println();
        IAccount john = service.openAccount("SAVINGS", "John Doe", 25, 15000);
        IAccount jane = service.openAccount("SAVINGS", "Jane Smith", 30, 10000);
        john.setPin(1234);
        jane.setPin(5678);
        System.out.println("[STEP 13] Opened: " + john.getAccountInfo());
        System.out.println("[STEP 13] Opened: " + jane.getAccountInfo());

        System.out.println();
        System.out.println("[STEP 14] Deposit: " + service.deposit(1001, 5000));
        System.out.println("[STEP 15] Withdrawal: " + service.withdraw(1001, 2000, 1234));
        System.out.println("[STEP 16] Transfer: " + service.transfer(1001, 1002, 1000, 1234));

        System.out.println("\n[STEP 17] Final Balances:");
        System.out.println("  John (Account #1001): Rs. " + service.getAccount(1001).getBalance());
        System.out.println("  Jane (Account #1002): Rs. " + service.getAccount(1002).getBalance());
        check(service.getAccount(1001).getBalance() == 17000, "John balance");
        check(service.getAccount(1002).getBalance() == 11000, "Jane balance");

        List<Transaction> history = service.getTransactionHistory();
        check(history.size() == 3, "history size");
        System.out.println("\n[STEP 18] Transaction History (" + history.size() + " records):");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("  [" + (index + 1) + "] " + history.get(index));
        }

        System.out.println("\n[STEP 19] Error Handling Checks:");
        try {
            service.deposit(9999, 100);
            throw new AssertionError("Expected missing account error");
        } catch (Exception e) {
            System.out.println("  Deposit to missing account caught: " + e.getMessage() + " [PASS]");
        }
        try {
            service.withdraw(1001, 100, 1111);
            throw new AssertionError("Expected wrong PIN error");
        } catch (Exception e) {
            System.out.println("  Withdrawal with wrong PIN caught: " + e.getMessage() + " [PASS]");
        }
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Check failed: " + label);
        }
    }
}
