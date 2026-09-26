package com.gdb.domain;

public final class AccountRulesEngine {

    private AccountRulesEngine() {
    }

    public static double getSavingsMinBalance(int tenureYears) {
        AccountRulesPropertiesLoader rules = SavingsRules.LOADER;
        return rules.getDouble("minBalance." + savingsTier(rules, tenureYears), 10000.0);
    }

    public static double getSavingsInterestRate(int tenureYears) {
        AccountRulesPropertiesLoader rules = SavingsRules.LOADER;
        return rules.getDouble("interestRate." + savingsTier(rules, tenureYears), 2.70);
    }

    public static double getCurrentOverdraftLimit(double monthlyTurnover) {
        AccountRulesPropertiesLoader rules = CurrentRules.LOADER;
        double minimum = rules.getDouble("overdraft.minimum", 25000.0);
        double turnoverMultiplier = rules.getDouble("overdraft.turnoverMultiplier", 2.5);
        return Math.max(minimum, monthlyTurnover * turnoverMultiplier);
    }

    public static double getFDInterestRate(int months) {
        AccountRulesPropertiesLoader rules = FixedDepositRules.LOADER;
        int privilegeThreshold = rules.getInt("tenure.privilege.minMonths", 12);
        int standardThreshold = rules.getInt("tenure.standard.minMonths", 6);
        String tier = months >= privilegeThreshold ? "privilege"
                : months >= standardThreshold ? "standard" : "new";
        return rules.getDouble("interestRate." + tier, 5.50);
    }

    public static boolean hasSalaryCreditHistory(int inactiveMonths) {
        return inactiveMonths <= SalaryRules.LOADER.getInt("creditHistory.maxInactiveMonths", 0);
    }

    private static String savingsTier(AccountRulesPropertiesLoader rules, int tenureYears) {
        if (tenureYears >= rules.getInt("tenure.privilege.minYears", 5)) {
            return "privilege";
        }
        if (tenureYears >= rules.getInt("tenure.premium.minYears", 3)) {
            return "premium";
        }
        if (tenureYears >= rules.getInt("tenure.standard.minYears", 1)) {
            return "standard";
        }
        return "new";
    }

    private static final class SavingsRules {
        private static final AccountRulesPropertiesLoader LOADER =
                new AccountRulesPropertiesLoader("config/rules/savings.properties");
    }

    private static final class CurrentRules {
        private static final AccountRulesPropertiesLoader LOADER =
                new AccountRulesPropertiesLoader("config/rules/current.properties");
    }

    private static final class FixedDepositRules {
        private static final AccountRulesPropertiesLoader LOADER =
                new AccountRulesPropertiesLoader("config/rules/fixeddeposit.properties");
    }

    private static final class SalaryRules {
        private static final AccountRulesPropertiesLoader LOADER =
                new AccountRulesPropertiesLoader("config/rules/salary.properties");
    }
}