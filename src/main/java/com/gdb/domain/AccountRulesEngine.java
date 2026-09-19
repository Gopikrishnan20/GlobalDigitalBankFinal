package com.gdb.domain;

public final class AccountRulesEngine {

    private AccountRulesEngine() {
    }

    // Simple if/else rules replace the previous TreeMap threshold lookup.
    public static double getSavingsMinBalance(int tenureYears) {
        if (tenureYears >= 5) {
            return 2500.0;
        } else if (tenureYears >= 3) {
            return 5000.0;
        } else if (tenureYears >= 1) {
            return 7500.0;
        }
        return 10000.0;
    }
    public static double getSavingsInterestRate(int tenureYears) {
        if (tenureYears >= 5) {
            return 4.00;
        } else if (tenureYears >= 3) {
            return 3.50;
        } else if (tenureYears >= 1) {
            return 3.00;
        }
        return 2.70;
    }
    public static double getCurrentOverdraftLimit(double monthlyTurnover) {
        return Math.max(25000.0, monthlyTurnover * 2.5);
    }
    public static double getFDInterestRate(int months) {
        if (months >= 12) {
            return 6.50;
        } else if (months >= 6) {
            return 6.00;
        }
        return 5.50;
    }
}
