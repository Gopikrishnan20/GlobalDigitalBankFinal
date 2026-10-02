package com.gdb.tests;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.domain.SavingsAccount;
import com.gdb.logging.TransactionLog;
import java.util.List;

public class TestCommandLogging {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 17 — COMMAND PATTERN + FILE LOGGING");
        System.out.println("=".repeat(60));

        SavingsAccount source = new SavingsAccount(1001, "Rajesh Sharma", 30, 15000);
        SavingsAccount destination = new SavingsAccount(1002, "Priya Patel", 28, 10000);
        source.setPin(1234);

        TransactionLog log = new TransactionLog();
        log.clear();
        executeAndLog(new DepositCommand(source, 5000), log, 11);
        executeAndLog(new WithdrawCommand(source, 2000, 1234), log, 12);
        executeAndLog(new TransferCommand(source, destination, 3000, 1234), log, 13);

        List<TransactionCommand> history = log.readAll();
        if (history.size() != 3) {
            throw new AssertionError("Expected 3 logged commands, found " + history.size());
        }
        System.out.println("\n[STEP 14] Read " + history.size() + " commands from transaction log:");
        for (int index = 0; index < history.size(); index++) {
            System.out.println("  [" + (index + 1) + "] " + history.get(index).getTransaction());
        }

        List<TransactionCommand> persistedHistory = new TransactionLog().readAll();
        if (persistedHistory.size() != 3) {
            throw new AssertionError("Fresh reader expected 3 commands, found " + persistedHistory.size());
        }
        System.out.println("\n[STEP 15] Fresh reader verified " + persistedHistory.size()
                + " persisted commands [PASS]");
    }

    private static void executeAndLog(TransactionCommand command, TransactionLog log, int step)
            throws Exception {
        command.execute();
        log.log(command);
        System.out.println("[STEP " + step + "] Logged: " + command.getTransaction());
    }
}