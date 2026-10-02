package com.gdb.tests;

import com.gdb.logging.MemoryLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import com.gdb.ui.AccountUI;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TestAccountUI {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 20 — ACCOUNT UI TEST");
        System.out.println("=".repeat(60));

        String script = String.join("\n",
                "1", "SAVINGS", "John Doe", "25", "15000", "1234",
                "1", "SAVINGS", "Jane Smith", "30", "10000", "5678",
                "2", "1001", "5000",
                "3", "1001", "2000", "1234",
                "4", "1001", "1002", "1000", "1234",
                "3", "1001", "100", "9999",
                "2", "9999", "100",
                "2", "abc",
                "9",
                "7",
                "0") + "\n";

        AccountService service = new AccountService(new TransactionLogger(new MemoryLogDestination()));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new AccountUI(service, new Scanner(script), capture).start();
        String output = buffer.toString(StandardCharsets.UTF_8);

        System.out.println();
        check(output.contains("Opened: Account #1001 | John Doe"), "Open account via menu");
        check(output.contains("Opened: Account #1002 | Jane Smith"), "Open second account via menu");
        check(output.contains("Deposit: [TXN-") && output.contains("Balance After: Rs. 20000.0"), "Deposit via menu");
        check(output.contains("Withdrawal: [TXN-") && output.contains("Balance After: Rs. 18000.0"), "Withdraw via menu");
        check(output.contains("Transfer: [TXN-") && output.contains("to Account #1002"), "Transfer via menu");
        check(output.contains("Error: Incorrect PIN"), "Wrong PIN reported");
        check(output.contains("Error: Account not found: 9999"), "Missing account reported");
        check(output.contains("Error: please enter a valid number"), "Bad number input reported");
        check(output.contains("Invalid option: 9"), "Invalid menu option reported");
        check(output.contains("Transaction History (3 records)"), "History shows 3 records");
        check(output.contains("Goodbye!"), "Exit option ends session");
        check(service.getAccount(1001).getBalance() == 17000, "John balance is Rs. 17000.0");
        check(service.getAccount(1002).getBalance() == 11000, "Jane balance is Rs. 11000.0");

        System.out.println("\nAll AccountUI checks passed!");
    }

    private static void check(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("FAILED: " + label);
        }
        System.out.println("  " + label + " [PASS]");
    }
}
