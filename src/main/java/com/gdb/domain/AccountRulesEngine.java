package com.gdb.domain;

import java.util.Locale;
import java.util.Map;

public final class AccountRulesEngine {

    private static final AccountRulesEngine INSTANCE = new AccountRulesEngine();
    private final Map<String, AccountRulesPropertiesLoader> ruleFiles;

    private AccountRulesEngine() {
        ruleFiles = Map.of(
                "savings", new AccountRulesPropertiesLoader("config/rules/savings.properties"),
                "current", new AccountRulesPropertiesLoader("config/rules/current.properties"),
                "fixeddeposit", new AccountRulesPropertiesLoader("config/rules/fixeddeposit.properties"),
                "salary", new AccountRulesPropertiesLoader("config/rules/salary.properties"));
    }

    public static AccountRulesEngine getInstance() {
        return INSTANCE;
    }

    public static double getSavingsMinBalance(int tenureYears) {
        AccountRulesPropertiesLoader rules = INSTANCE.ruleFiles.get("savings");
        return rules.getDouble("minBalance." + savingsTier(rules, tenureYears), 10000.0);
    }

    public static double getSavingsInterestRate(int tenureYears) {
        AccountRulesPropertiesLoader rules = INSTANCE.ruleFiles.get("savings");
        return rules.getDouble("interestRate." + savingsTier(rules, tenureYears), 2.70);
    }

    public static double getCurrentOverdraftLimit(double monthlyTurnover) {
        AccountRulesPropertiesLoader rules = INSTANCE.ruleFiles.get("current");
        double minimum = rules.getDouble("overdraft.minimum", 25000.0);
        double turnoverMultiplier = rules.getDouble("overdraft.turnoverMultiplier", 2.5);
        return Math.max(minimum, monthlyTurnover * turnoverMultiplier);
    }

    public static double getFDInterestRate(int months) {
        AccountRulesPropertiesLoader rules = INSTANCE.ruleFiles.get("fixeddeposit");
        int privilegeThreshold = rules.getInt("tenure.privilege.minMonths", 12);
        int standardThreshold = rules.getInt("tenure.standard.minMonths", 6);
        String tier = months >= privilegeThreshold ? "privilege"
                : months >= standardThreshold ? "standard" : "new";
        return rules.getDouble("interestRate." + tier, 5.50);
    }

    public static boolean hasSalaryCreditHistory(int inactiveMonths) {
        return inactiveMonths <= INSTANCE.ruleFiles.get("salary")
                .getInt("creditHistory.maxInactiveMonths", 0);
    }

    public double getDailyTransferLimit(String accountType, int tenureYears) {
        Object feature = getAdditionalFeature(accountType, tenureYears, "dailyTransferLimit");
        return feature == null ? 0.0 : (Double) feature;
    }

    public Object getAdditionalFeature(String accountType, int tenureYears, String featureKey) {
        if (accountType == null || featureKey == null) {
            return null;
        }
        String normalizedType = accountType.toLowerCase(Locale.ROOT).replace("_", "");
        if (normalizedType.equals("fd")) {
            normalizedType = "fixeddeposit";
        }
        AccountRulesPropertiesLoader rules = ruleFiles.get(normalizedType);
        if (rules == null) {
            return null;
        }
        String tier = savingsTier(rules, tenureYears);
        return rules.getAdditionalFeature(featureKey, tier);
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

}