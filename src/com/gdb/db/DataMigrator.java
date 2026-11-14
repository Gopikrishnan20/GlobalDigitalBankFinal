package com.gdb.db;

import com.gdb.command.TransactionCommand;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.logging.LogDestination;
import com.gdb.repository.AccountRepository;
import com.gdb.repository.TransactionRepository;

/**
 * Utility for migrating legacy in-memory or file data into another repository, such as SQLite.
 */
public class DataMigrator {

    /**
     * Copies every transaction recorded in a command log into the target repository.
     * @return number of transaction records migrated
     */
    public static int migrate(LogDestination source, TransactionRepository target) {
        int migrated = 0;
        for (TransactionCommand command : source.readAll()) {
            Transaction transaction = command.getTransaction();
            if (transaction != null) {
                target.save(transaction);
                migrated++;
            }
        }
        return migrated;
    }

    /**
     * Copies all accounts, then all transactions, from source repositories to destination repositories.
     * Accounts go first so transaction rows always refer to accounts that already exist.
     */
    public static MigrationResult migrate(AccountRepository sourceAccounts, AccountRepository targetAccounts,
                                          TransactionRepository sourceTransactions, TransactionRepository targetTransactions) {
        int accounts = 0;
        for (IAccount account : sourceAccounts.findAll()) {
            if (!targetAccounts.exists(account.getAccountNumber())) {
                targetAccounts.save(account);
                accounts++;
            }
        }
        int transactions = 0;
        for (Transaction transaction : sourceTransactions.findAll()) {
            targetTransactions.save(transaction);
            transactions++;
        }
        return new MigrationResult(accounts, transactions);
    }

    public static class MigrationResult {
        private final int accountsMigrated;
        private final int transactionsMigrated;

        public MigrationResult(int accountsMigrated, int transactionsMigrated) {
            this.accountsMigrated = accountsMigrated;
            this.transactionsMigrated = transactionsMigrated;
        }

        public int getAccountsMigrated() { return accountsMigrated; }
        public int getTransactionsMigrated() { return transactionsMigrated; }

        @Override
        public String toString() {
            return "Migrated " + accountsMigrated + " accounts and " + transactionsMigrated + " transaction records.";
        }
    }
}
