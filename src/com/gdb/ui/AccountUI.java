package com.gdb.ui;

import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.service.AccountService;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class AccountUI {
    private final AccountService service;
    private final Scanner scanner;
    private final PrintStream out;

    public AccountUI(AccountService service) {
        this(service, new Scanner(System.in), System.out);
    }

    public AccountUI(AccountService service, Scanner scanner, PrintStream out) {
        this.service = service;
        this.scanner = scanner;
        this.out = out;
    }

    public void start() {
        out.println("=".repeat(50));
        out.println("  Welcome to Global Digital Bank");
        out.println("=".repeat(50));

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Choose an option");
            if (choice == null) {
                break;
            }
            try {
                switch (choice) {
                    case "1": openAccount(); break;
                    case "2": deposit(); break;
                    case "3": withdraw(); break;
                    case "4": transfer(); break;
                    case "5": viewAccount(); break;
                    case "6": listAccounts(); break;
                    case "7": showHistory(); break;
                    case "8": closeAccount(); break;
                    case "0": running = false; break;
                    default: out.println("Invalid option: " + choice);
                }
            } catch (NumberFormatException e) {
                out.println("Error: please enter a valid number");
            } catch (Exception e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Thank you for banking with Global Digital Bank. Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("1. Open Account");
        out.println("2. Deposit");
        out.println("3. Withdraw");
        out.println("4. Transfer");
        out.println("5. View Account");
        out.println("6. List All Accounts");
        out.println("7. Transaction History");
        out.println("8. Close Account");
        out.println("0. Exit");
    }

    private void openAccount() throws Exception {
        String type = prompt("Account type (SAVINGS/CURRENT/FIXEDDEPOSIT)");
        String name = prompt("Holder name");
        int age = readInt("Age");
        double balance = readDouble("Initial balance");
        int pin = readInt("Set 4-digit PIN");
        IAccount account = service.openAccount(type, name, age, balance);
        account.setPin(pin);
        out.println("Opened: " + account.getAccountInfo());
    }

    private void deposit() throws Exception {
        int accountNumber = readInt("Account number");
        double amount = readDouble("Amount");
        out.println("Deposit: " + service.deposit(accountNumber, amount));
    }

    private void withdraw() throws Exception {
        int accountNumber = readInt("Account number");
        double amount = readDouble("Amount");
        int pin = readInt("PIN");
        out.println("Withdrawal: " + service.withdraw(accountNumber, amount, pin));
    }

    private void transfer() throws Exception {
        int from = readInt("From account");
        int to = readInt("To account");
        double amount = readDouble("Amount");
        int pin = readInt("PIN");
        out.println("Transfer: " + service.transfer(from, to, amount, pin));
    }

    private void viewAccount() {
        int accountNumber = readInt("Account number");
        IAccount account = service.getAccount(accountNumber);
        out.println(account == null ? "Account not found: " + accountNumber : account.getAccountInfo());
    }

    private void listAccounts() {
        List<IAccount> accounts = service.getAllAccounts();
        if (accounts.isEmpty()) {
            out.println("No accounts yet.");
        }
        for (IAccount account : accounts) {
            out.println("  " + account.getAccountInfo());
        }
    }

    private void showHistory() {
        List<Transaction> history = service.getTransactionHistory();
        out.println("Transaction History (" + history.size() + " records):");
        for (int index = 0; index < history.size(); index++) {
            out.println("  [" + (index + 1) + "] " + history.get(index));
        }
    }

    private void closeAccount() throws Exception {
        int accountNumber = readInt("Account number");
        int pin = readInt("PIN");
        service.closeAccount(accountNumber, pin);
        out.println("Account #" + accountNumber + " closed.");
    }

    private String prompt(String label) {
        out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    private int readInt(String label) {
        return Integer.parseInt(prompt(label));
    }

    private double readDouble(String label) {
        return Double.parseDouble(prompt(label));
    }
}
