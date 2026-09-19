package com.gdb.domain;

public final class AccountFactory {

    private AccountFactory() {
    }

    public static AbstractAccount createAccount(String accountType,
                                                String accountNumber,
                                                String name,
                                                int age,
                                                double balance,
                                                String status,
                                                String pin,
                                                Object... typeSpecificParameters) {
        if (accountType == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }

        switch (accountType.toUpperCase()) {
            case "SAVINGS":
                return new SavingsAccount(accountNumber, name, age, balance, status, pin,
                        parameter(typeSpecificParameters, 0, 0.0),
                        parameter(typeSpecificParameters, 1, 0.0));
            case "CURRENT":
                return new CurrentAccount(accountNumber, name, age, balance, status, pin,
                        parameter(typeSpecificParameters, 0, 0.0));
            case "FIXED_DEPOSIT":
            case "FD":
                return new FixedDepositAccount(accountNumber, name, age, balance, status, pin,
                        (int) parameter(typeSpecificParameters, 0, 0.0),
                        parameter(typeSpecificParameters, 1, 0.0));
            case "SALARY":
                return new SalaryAccount(accountNumber, name, age, balance, status, pin,
                        stringParameter(typeSpecificParameters, 0, ""),
                        (int) parameter(typeSpecificParameters, 1, 0.0));
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }

    private static double parameter(Object[] parameters, int index, double defaultValue) {
        if (index >= parameters.length || parameters[index] == null) {
            return defaultValue;
        }
        if (!(parameters[index] instanceof Number number)) {
            throw new IllegalArgumentException("Account parameter must be numeric");
        }
        return number.doubleValue();
    }

    private static String stringParameter(Object[] parameters, int index, String defaultValue) {
        if (index >= parameters.length || parameters[index] == null) {
            return defaultValue;
        }
        if (!(parameters[index] instanceof String value)) {
            throw new IllegalArgumentException("Account parameter must be text");
        }
        return value;
    }
}
