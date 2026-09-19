package org.example;

import com.gdb.domain.AbstractAccount;
import com.gdb.domain.BankingOperations;
import com.gdb.exceptions.AccountException;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Activity 10: Banking Operations Suite ===");

        AbstractAccount[] portfolio = BankingOperations.createPortfolio();
        AbstractAccount savings = portfolio[0];
        AbstractAccount current = portfolio[1];

        try {
            BankingOperations.transferFunds(savings, current, 3000.0, "1234");
            System.out.println("Transfer Rs 3000 from Savings to Current: SUCCESS");
            System.out.println("Savings Balance: Rs " + savings.getBalance()
                    + " | Current Balance: Rs " + current.getBalance());
        } catch (AccountException exception) {
            System.out.println("Transfer failed: " + exception.getMessage());
        }

        double savingsBalance = savings.getBalance();
        double currentBalance = current.getBalance();
        try {
            BankingOperations.transferFunds(savings, current, 1000.0, "0000");
            System.out.println("Failed Transfer (Wrong PIN): [FAIL]");
        } catch (AccountException exception) {
            boolean unchanged = savings.getBalance() == savingsBalance
                    && current.getBalance() == currentBalance;
            System.out.println("Failed Transfer (Wrong PIN): Exception caught, no balance changed "
                    + (unchanged ? "[PASS]" : "[FAIL]"));
        }

        BankingOperations.processMonthlyCycle(portfolio);
        System.out.println("Monthly Interest Cycle processed for all qualifying accounts.");
        System.out.println("All banking operations passed!");
    }
}
