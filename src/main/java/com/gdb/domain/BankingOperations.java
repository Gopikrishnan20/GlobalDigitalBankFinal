package com.gdb.domain;

import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidAmountException;

public final class BankingOperations {

    private BankingOperations() {
    }

    public static AbstractAccount[] createPortfolio() {
        return new AbstractAccount[] {
            new SavingsAccount("SAV001", "Raj Kumar", 28, 10000.0,
                    "ACTIVE", "1234", 1000.0, 4.0),
            new CurrentAccount("CUR001", "Priya Singh", 32, 5000.0,
                    "ACTIVE", "5678", 25000.0),
            new SalaryAccount("SAL001", "Amit Patel", 30, 7000.0,
                    "ACTIVE", "2468", "Global Digital Bank", 0)
        };
    }

    public static void transferFunds(AbstractAccount source, AbstractAccount destination,
                                     double amount, String pin) throws AccountException {
        source.withdraw(amount, pin);
        try {
            destination.deposit(amount);
        } catch (InvalidAmountException exception) {
            try {
                source.deposit(amount);
            } catch (InvalidAmountException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw exception;
        }
    }

    public static void processMonthlyCycle(AbstractAccount[] portfolio) {
        for (AbstractAccount account : portfolio) {
            if (account instanceof SavingsAccount savingsAccount) {
                savingsAccount.applyInterest();
            } else if (account instanceof SalaryAccount salaryAccount) {
                salaryAccount.hasSalaryCreditHistory();
            }
        }
    }
}