package com.gdb;

import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.logging.FileLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;

public class Main {
    public static void main(String[] args) throws Exception {
        // STEP 20: Set up service with file-backed logging
        TransactionLogger logger = new TransactionLogger(new FileLogDestination());
        logger.clear();
        AccountService service = new AccountService(logger);

        // STEP 21: Demo workflow
        IAccount alice = service.openAccount("SAVINGS", "Alice Kumar", 35, 20000);
        IAccount bob = service.openAccount("CURRENT", "Bob Menon", 40, 30000);
        alice.setPin(4321);
        bob.setPin(8765);

        System.out.println("Global Digital Bank — Service Layer Demo");
        System.out.println("Opened: " + alice.getAccountInfo());
        System.out.println("Opened: " + bob.getAccountInfo());

        service.deposit(alice.getAccountNumber(), 3000);
        service.withdraw(bob.getAccountNumber(), 1500, 8765);
        service.transfer(alice.getAccountNumber(), bob.getAccountNumber(), 2500, 4321);

        System.out.println("\nTransaction history (" + logger.getDestinationName() + "):");
        for (Transaction transaction : service.getTransactionHistory()) {
            System.out.println("  " + transaction);
        }

        System.out.println("\nFinal accounts:");
        for (IAccount account : service.getAllAccounts()) {
            System.out.println("  " + account.getAccountInfo());
        }
    }
}
