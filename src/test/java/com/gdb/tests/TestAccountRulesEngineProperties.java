package com.gdb.tests;

import com.gdb.domain.AccountRulesEngine;

public class TestAccountRulesEngineProperties {

    public static void main(String[] args) {
        System.out.println("=== Activity 14: Properties-Driven Rules Engine Test ===");

        int[] tenures = {0, 2, 4, 6};
        double[] minimumBalances = {10000.0, 7500.0, 5000.0, 2500.0};
        double[] interestRates = {2.70, 3.00, 3.50, 4.00};
        double[] dailyTransferLimits = {50000.0, 100000.0, 200000.0, 500000.0};

        for (int index = 0; index < tenures.length; index++) {
            int tenure = tenures[index];
            double minimumBalance = AccountRulesEngine.getSavingsMinBalance(tenure);
            double interestRate = AccountRulesEngine.getSavingsInterestRate(tenure);
                double dailyTransferLimit = AccountRulesEngine.getInstance()
                    .getDailyTransferLimit("SAVINGS", tenure);
                if (minimumBalance != minimumBalances[index] || interestRate != interestRates[index]
                    || dailyTransferLimit != dailyTransferLimits[index]) {
                throw new AssertionError("Unexpected savings rules for tenure " + tenure);
            }
                System.out.printf("Tenure %d yrs -> Min Balance: Rs %.1f | Interest: %.2f%%"
                        + " | Daily Transfer Limit: Rs %.1f%n",
                    tenure, minimumBalance, interestRate, dailyTransferLimit);
        }

        if (AccountRulesEngine.getCurrentOverdraftLimit(20000.0) != 50000.0
                || AccountRulesEngine.getFDInterestRate(12) != 6.50
                || !AccountRulesEngine.hasSalaryCreditHistory(0)
                || AccountRulesEngine.hasSalaryCreditHistory(1)) {
            throw new AssertionError("Unexpected account rules from properties");
        }

        System.out.println("All external properties loaded and verified successfully!");
    }
}