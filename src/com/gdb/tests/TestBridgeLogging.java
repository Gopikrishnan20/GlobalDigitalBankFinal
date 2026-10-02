package com.gdb.tests;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.domain.SavingsAccount;
import com.gdb.logging.DatabaseLogDestination;
import com.gdb.logging.FileLogDestination;
import com.gdb.logging.LogDestination;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.SimulatedDatabase;
import com.gdb.logging.TransactionLogger;

public class TestBridgeLogging {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 18 — BRIDGE PATTERN (FILE + DB)");
        System.out.println("=".repeat(60));

        LogDestination file = new FileLogDestination();
        LogDestination database = new DatabaseLogDestination(new SimulatedDatabase());
        LogDestination memory = new MemoryLogDestination();
        file.clear();

        SavingsAccount source = new SavingsAccount(1001, "Rajesh Sharma", 30, 15000);
        SavingsAccount target = new SavingsAccount(1002, "Priya Patel", 28, 10000);
        source.setPin(1234);

        TransactionLogger logger = new TransactionLogger(file);

        System.out.println("\n[STEP 25] Logging to FILE destination...");
        logSampleTransactions(logger, source, target);
        System.out.println("  FILE log count: " + logger.readAll().size());

        logger.setDestination(database);
        System.out.println("\n[STEP 26] Switched to DATABASE destination...");
        logSampleTransactions(logger, source, target);
        System.out.println("  DATABASE log count: " + logger.readAll().size());

        logger.setDestination(memory);
        System.out.println("\n[STEP 27] Switched to MEMORY destination...");
        logSampleTransactions(logger, source, target);
        System.out.println("  MEMORY log count: " + logger.readAll().size());

        System.out.println("\n[STEP 28] Verifying Data Isolation:");
        verifyCount(file, 3);
        verifyCount(database, 3);
        verifyCount(memory, 3);

        System.out.println("\n[STEP 29] All Bridge Pattern log backends verified successfully!");
    }

    private static void logSampleTransactions(TransactionLogger logger, SavingsAccount source,
            SavingsAccount target) throws Exception {
        TransactionCommand[] commands = {
                new DepositCommand(source, 5000),
                new WithdrawCommand(source, 2000, 1234),
                new TransferCommand(source, target, 3000, 1234)
        };
        for (TransactionCommand command : commands) {
            command.execute();
            logger.log(command);
        }
    }

    private static void verifyCount(LogDestination destination, int expected) {
        int actual = destination.readAll().size();
        if (actual != expected) {
            throw new AssertionError(destination.getDestinationName() + " expected " + expected
                    + " entries, found " + actual);
        }
        System.out.println("  " + destination.getDestinationName() + " count: " + actual
                + " [EXPECTED: " + expected + "]");
    }
}
