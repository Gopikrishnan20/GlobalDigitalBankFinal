package com.gdb.tests;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.db.ConnectionProvider;
import com.gdb.db.DataMigrator;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;
import com.gdb.logging.MemoryLogDestination;
import com.gdb.repository.InMemoryAccountRepository;
import com.gdb.repository.InMemoryTransactionRepository;
import com.gdb.repository.JdbcAccountRepository;
import com.gdb.repository.JdbcTransactionRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class TestJdbcTransactionRepository {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 24 — JDBC TRANSACTION REPOSITORY & MIGRATION");
        System.out.println("=".repeat(60));

        File dbFile = new File("test_txn_gdb.db");
        if (dbFile.exists()) dbFile.delete();

        ConnectionProvider provider = new JdbcConnectionProvider("jdbc:sqlite:test_txn_gdb.db");
        SchemaInitializer.initialize(provider);
        JdbcAccountRepository accountRepo = new JdbcAccountRepository(provider);
        JdbcTransactionRepository txnRepo = new JdbcTransactionRepository(provider);

        accountRepo.save(AccountFactory.createAccount("SAVINGS", 1001, "Rajesh Sharma", 30, 50000.0));
        accountRepo.save(AccountFactory.createAccount("SAVINGS", 1002, "Priya Patel", 28, 30000.0));

        System.out.println("[TEST 1] Transaction Save & Query:");
        txnRepo.save(new Transaction(Transaction.generateId(), LocalDateTime.now(), 1001, TransactionType.DEPOSIT,
                5000.0, 55000.0, "SUCCESS", "Deposit of Rs. 5000.0", 0, 0));
        txnRepo.save(new Transaction(Transaction.generateId(), LocalDateTime.now(), 1001, TransactionType.WITHDRAW,
                5000.0, 50000.0, "SUCCESS", "Withdrawal of Rs. 5000.0", 0, 0));
        System.out.println("  Saved 2 transaction records.");
        List<Transaction> found = txnRepo.findByAccount(1001);
        System.out.println("  Found transactions for Account #1001: " + found.size());
        check(found.size() == 2, "Account 1001 should have 2 transactions");
        check(found.get(0).getType() == TransactionType.DEPOSIT && found.get(0).getAmount() == 5000.0,
                "First transaction fields should round-trip");
        check(txnRepo.findByAccount(1002).isEmpty(), "Account 1002 should have no transactions yet");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 2] Atomic Transfer Execution:");
        System.out.println("  Initial Sender Balance: Rs. " + balance(accountRepo, 1001)
                + ", Receiver Balance: Rs. " + balance(accountRepo, 1002));
        System.out.println("  Executing atomic transfer of Rs. 10000.0...");
        txnRepo.transfer(1001, 1002, 10000.0);
        System.out.println("  Updated Sender Balance: Rs. " + balance(accountRepo, 1001));
        System.out.println("  Updated Receiver Balance: Rs. " + balance(accountRepo, 1002));
        check(balance(accountRepo, 1001) == 40000.0, "Sender should be debited");
        check(balance(accountRepo, 1002) == 40000.0, "Receiver should be credited");
        List<Transaction> receiverHistory = txnRepo.findByAccount(1002);
        check(receiverHistory.size() == 1 && receiverHistory.get(0).getType() == TransactionType.TRANSFER,
                "Transfer record should be visible to the receiver");
        System.out.println("  Transfer audit transaction recorded in database.");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 3] Atomic Rollback on Insufficient Balance:");
        int recordsBefore = txnRepo.findAll().size();
        System.out.println("  Attempting transfer of Rs. 999999.0 (exceeds balance)...");
        check(throwsDataAccess(() -> txnRepo.transfer(1001, 1002, 999999.0)), "Insufficient balance should fail");
        check(throwsDataAccess(() -> txnRepo.transfer(1001, 9999, 100.0)), "Missing receiver should fail");
        check(throwsDataAccess(() -> txnRepo.transfer(1001, 1002, -1.0)), "Negative amount should fail");
        System.out.println("  Sender Balance unchanged: Rs. " + balance(accountRepo, 1001));
        System.out.println("  Receiver Balance unchanged: Rs. " + balance(accountRepo, 1002));
        check(balance(accountRepo, 1001) == 40000.0 && balance(accountRepo, 1002) == 40000.0,
                "Failed transfers must not change balances");
        check(txnRepo.findAll().size() == recordsBefore, "Failed transfers must not write audit records");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 4] Data Migration from Memory to JDBC:");
        InMemoryAccountRepository memAccounts = new InMemoryAccountRepository();
        InMemoryTransactionRepository memTxns = new InMemoryTransactionRepository();
        IAccount a = AccountFactory.createAccount("SAVINGS", 2001, "Anita Rao", 35, 20000.0);
        IAccount b = AccountFactory.createAccount("SAVINGS", 2002, "Vikram Singh", 40, 25000.0);
        IAccount c = AccountFactory.createAccount("CURRENT", 2003, "Meera Iyer", 32, 60000.0);
        a.setPin(1111);
        memAccounts.save(a);
        memAccounts.save(b);
        memAccounts.save(c);
        for (int i = 1; i <= 5; i++) {
            memTxns.save(new Transaction(Transaction.generateId(), LocalDateTime.now(), 2001, TransactionType.DEPOSIT,
                    100.0 * i, 20000.0 + 100.0 * i, "SUCCESS", "Legacy deposit #" + i, 0, 0));
        }
        int totalBefore = txnRepo.findAll().size();
        DataMigrator.MigrationResult result = DataMigrator.migrate(memAccounts, accountRepo, memTxns, txnRepo);
        System.out.println("  " + result);
        check(result.getAccountsMigrated() == 3 && result.getTransactionsMigrated() == 5, "Should migrate 3 accounts and 5 records");
        check(accountRepo.exists(2001) && accountRepo.exists(2002) && accountRepo.exists(2003), "Migrated accounts should exist");
        check(accountRepo.findById(2003).getBalance() == 60000.0, "Migrated balance should match");
        check(accountRepo.findById(2001).verifyPin(1111), "Migrated PIN should match");
        check(txnRepo.findByAccount(2001).size() == 5, "Account 2001 should have 5 migrated records");
        check(txnRepo.findAll().size() == totalBefore + 5, "Total records should grow by 5");
        System.out.println("  Verified migrated accounts in SQLite database.");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 5] Migrating a Command Log into JDBC:");
        MemoryLogDestination log = new MemoryLogDestination();
        DepositCommand deposit = new DepositCommand(a, 1000.0);
        WithdrawCommand withdraw = new WithdrawCommand(a, 500.0, 1111);
        TransferCommand transfer = new TransferCommand(a, b, 200.0, 1111);
        deposit.execute();
        withdraw.execute();
        transfer.execute();
        log.write(deposit);
        log.write(withdraw);
        log.write(transfer);
        int migrated = DataMigrator.migrate(log, txnRepo);
        System.out.println("  Migrated " + migrated + " logged commands into the transactions table.");
        check(migrated == 3, "All 3 logged commands should migrate");
        check(txnRepo.findByAccount(2001).size() == 8, "Account 2001 should now have 8 records");
        check(txnRepo.findByAccount(2002).size() == 1, "Transfer should appear in receiver 2002's history");
        System.out.println("  -> PASSED");

        provider.shutdown();
        if (dbFile.exists()) dbFile.delete();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ALL ACTIVITY 24 JDBC TRANSACTION TESTS PASSED!");
        System.out.println("=".repeat(60));
    }

    private static double balance(JdbcAccountRepository repo, int accountNumber) {
        return repo.findById(accountNumber).getBalance();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean throwsDataAccess(Runnable action) {
        try {
            action.run();
            return false;
        } catch (DataAccessException e) {
            System.out.println("  Caught expected exception: " + e.getMessage());
            return true;
        }
    }
}
