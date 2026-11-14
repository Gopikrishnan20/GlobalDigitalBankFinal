package com.gdb.file;

import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.CurrentAccount;
import com.gdb.domain.FixedDepositAccount;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.DataAccessException;
import com.gdb.exceptions.MinimumBalanceViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Mapper between domain objects and CSV rows.
 * Fields containing a comma, quote or newline are wrapped in quotes, with inner quotes doubled.
 */
public class CsvMapper {

    public static final String ACCOUNT_HEADER = "account_number,account_holder_name,age,balance,account_type,status,pin,opening_date,overdraft_used,interest_earned,tenure_years";
    public static final String TRANSACTION_HEADER = "transaction_id,timestamp,account_number,type,amount,balance_after,status,from_account,to_account,description";

    public static String toCsv(IAccount account) {
        Account acc = (Account) account;
        double overdraftUsed = acc instanceof CurrentAccount ? ((CurrentAccount) acc).getOverdraftUsed() : 0.0;
        double interestEarned = acc instanceof FixedDepositAccount ? ((FixedDepositAccount) acc).getInterestEarned() : 0.0;
        return join(
            String.valueOf(acc.getAccountNumber()),
            acc.getAccountHolderName(),
            String.valueOf(acc.getAge()),
            String.valueOf(acc.getBalance()),
            acc.getAccountType(),
            acc.isActive() ? "Active" : "Inactive",
            acc.getPin() == null ? "" : String.valueOf(acc.getPin()),
            acc.getOpeningDate(),
            String.valueOf(overdraftUsed),
            String.valueOf(interestEarned),
            String.valueOf(acc.getTenureYears()));
    }

    public static IAccount fromCsv(String line) {
        List<String> f = split(line);
        if (f.size() < 10) {
            throw new DataAccessException("Invalid account CSV row: " + line);
        }
        try {
            int accountNumber = Integer.parseInt(f.get(0));
            String name = f.get(1);
            int age = Integer.parseInt(f.get(2));
            double balance = Double.parseDouble(f.get(3));
            String type = f.get(4);
            int tenure = f.size() > 10 && !f.get(10).isEmpty() ? Integer.parseInt(f.get(10)) : 0;

            Account account;
            try {
                account = (Account) AccountFactory.createAccount(type, accountNumber, name, age, balance, tenure);
            } catch (MinimumBalanceViolationException e) {
                // A stored balance may be below today's opening minimum (e.g. after withdrawals); rebuild, then restore it.
                account = (Account) AccountFactory.createAccount(type, accountNumber, name, age, Double.MAX_VALUE / 4, tenure);
            }
            account.restoreState(balance, f.get(7));
            if (!f.get(6).isEmpty()) {
                account.setPin(Integer.parseInt(f.get(6)));
            }
            if (!"Active".equalsIgnoreCase(f.get(5))) {
                account.closeAccount();
            }
            return account;
        } catch (AccountException | NumberFormatException e) {
            throw new DataAccessException("Invalid account CSV row: " + line + " (" + e.getMessage() + ")", e);
        }
    }

    public static String toCsv(Transaction t) {
        return join(
            t.getTransactionId(),
            t.getTimestamp() == null ? "" : t.getTimestamp().toString(),
            String.valueOf(t.getAccountNumber()),
            t.getType().name(),
            String.valueOf(t.getAmount()),
            String.valueOf(t.getBalanceAfter()),
            t.getStatus(),
            String.valueOf(t.getFromAccount()),
            String.valueOf(t.getToAccount()),
            t.getDescription());
    }

    public static Transaction fromCsvTransaction(String line) {
        List<String> f = split(line);
        if (f.size() < 10) {
            throw new DataAccessException("Invalid transaction CSV row: " + line);
        }
        try {
            return new Transaction(
                f.get(0),
                f.get(1).isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(f.get(1)),
                Integer.parseInt(f.get(2)),
                TransactionType.valueOf(f.get(3)),
                Double.parseDouble(f.get(4)),
                Double.parseDouble(f.get(5)),
                f.get(6),
                f.get(9),
                Integer.parseInt(f.get(7)),
                Integer.parseInt(f.get(8)));
        } catch (RuntimeException e) {
            throw new DataAccessException("Invalid transaction CSV row: " + line + " (" + e.getMessage() + ")", e);
        }
    }

    private static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
