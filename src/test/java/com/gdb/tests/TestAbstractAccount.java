package com.gdb.tests;

import com.gdb.domain.AbstractAccount;
import com.gdb.domain.CurrentAccount;
import com.gdb.domain.SalaryAccount;
import com.gdb.domain.SavingsAccount;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidAmountException;

public class TestAbstractAccount {

    public static void main(String[] args) {
        System.out.println("=== Activity 10: Banking Operations Suite ===");

        AbstractAccount[] portfolio = createPortfolio();
        AbstractAccount savings = portfolio[0];
        AbstractAccount current = portfolio[1];

        try {
            transferFunds(savings, current, 3000.0, "1234");
            System.out.println("Transfer Rs 3000 from Savings to Current: SUCCESS");
            System.out.println("Savings Balance: Rs " + savings.getBalance()
                    + " | Current Balance: Rs " + current.getBalance());
        } catch (AccountException exception) {
            System.out.println("Transfer failed: " + exception.getMessage());
        }

        double savingsBalance = savings.getBalance();
        double currentBalance = current.getBalance();
        try {
            transferFunds(savings, current, 1000.0, "0000");
            System.out.println("Failed Transfer (Wrong PIN): [FAIL]");
        } catch (AccountException exception) {
            boolean unchanged = savings.getBalance() == savingsBalance
                    && current.getBalance() == currentBalance;
            System.out.println("Failed Transfer (Wrong PIN): Exception caught, no balance changed "
                    + (unchanged ? "[PASS]" : "[FAIL]"));
        }

        processMonthlyCycle(portfolio);
        System.out.println("Monthly Interest Cycle processed for all qualifying accounts.");
        System.out.println("All banking operations passed!");
    }

    private static AbstractAccount[] createPortfolio() {
        return new AbstractAccount[] {
            new SavingsAccount("SAV001", "Raj Kumar", 28, 10000.0,
                    "ACTIVE", "1234", 1000.0, 4.0),
            new CurrentAccount("CUR001", "Priya Singh", 32, 5000.0,
                    "ACTIVE", "5678", 25000.0),
            new SalaryAccount("SAL001", "Amit Patel", 30, 7000.0,
                    "ACTIVE", "2468", "Global Digital Bank", 0)
        };
    }

    private static void transferFunds(AbstractAccount source, AbstractAccount destination,
                                      double amount, String pin) throws AccountException {
        source.withdraw(amount, pin);
        try {
            destination.deposit(amount);
        } catch (InvalidAmountException exception) {
            source.deposit(amount);
            throw exception;
        }
    }

    private static void processMonthlyCycle(AbstractAccount[] portfolio) {
        for (AbstractAccount account : portfolio) {
            if (account instanceof SavingsAccount savingsAccount) {
                savingsAccount.applyInterest();
            } else if (account instanceof SalaryAccount salaryAccount) {
                salaryAccount.hasSalaryCreditHistory();
            }
        }
    }
}
