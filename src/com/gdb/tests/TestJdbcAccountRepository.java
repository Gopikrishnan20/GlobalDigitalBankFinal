package com.gdb.tests;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.exceptions.DataAccessException;
import com.gdb.repository.JdbcAccountRepository;

import java.io.File;
import java.util.List;

public class TestJdbcAccountRepository {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 23 — JDBC ACCOUNT REPOSITORY CRUD TESTS");
        System.out.println("=".repeat(60));

        String testDbUrl = "jdbc:sqlite:test_acc_gdb.db";
        File dbFile = new File("test_acc_gdb.db");
        if (dbFile.exists()) dbFile.delete();

        ConnectionProvider provider = new JdbcConnectionProvider(testDbUrl);
        SchemaInitializer.initialize(provider);
        JdbcAccountRepository repo = new JdbcAccountRepository(provider);

        // TEST 1: Save & FindById
        System.out.println("[TEST 1] Save & FindById:");
        IAccount acc1 = AccountFactory.createAccount("SAVINGS", 1001, "Rajesh Sharma", 30, 50000.0);
        acc1.setPin(1234);
        repo.save(acc1);

        IAccount found = repo.findById(1001);
        System.out.println("  Found: " + found.getAccountInfo());
        assert found != null : "Account 1001 should be found";
        assert found.getAccountNumber() == 1001 : "Account number should match";
        assert "Rajesh Sharma".equals(found.getAccountHolderName()) : "Name should match";
        assert found.getBalance() == 50000.0 : "Balance should match";
        System.out.println("  -> PASSED");

        // TEST 2: Update & FindAll
        System.out.println("\n[TEST 2] Update & FindAll:");
        IAccount acc2 = AccountFactory.createAccount("CURRENT", 1002, "Priya Patel", 28, 40000.0);
        repo.save(acc2);

        found.deposit(15000.0);
        repo.update(found);

        IAccount updated = repo.findById(1001);
        System.out.println("  Updated balance: Rs. " + updated.getBalance() + " (Expected: 65000.0)");
        assert updated.getBalance() == 65000.0 : "Balance should be updated in DB";

        List<IAccount> all = repo.findAll();
        System.out.println("  Total accounts in DB: " + all.size() + " (Expected: 2)");
        assert all.size() == 2 : "Should have 2 accounts";
        System.out.println("  -> PASSED");

        // TEST 3: Delete & Exists
        System.out.println("\n[TEST 3] Delete & Exists:");
        assert repo.exists(1002) : "Account 1002 should exist";
        repo.delete(1002);
        assert !repo.exists(1002) : "Account 1002 should no longer exist";
        assert repo.findById(1002) == null : "findById(1002) should return null";
        System.out.println("  Account 1002 successfully deleted.");
        System.out.println("  -> PASSED");

        // TEST 4: Atomic Transfer (commit)
        System.out.println("\n[TEST 4] Atomic Transfer (commit):");
        IAccount acc3 = AccountFactory.createAccount("SAVINGS", 1003, "Anita Rao", 35, 20000.0);
        repo.save(acc3);
        repo.transfer(1001, 1003, 5000.0);
        double b1 = repo.findById(1001).getBalance();
        double b3 = repo.findById(1003).getBalance();
        System.out.println("  1001: Rs. " + b1 + " (Expected: 60000.0), 1003: Rs. " + b3 + " (Expected: 25000.0)");
        check(b1 == 60000.0 && b3 == 25000.0, "Transfer should debit and credit both accounts");
        System.out.println("  -> PASSED");

        // TEST 5: Rollback on insufficient balance / missing account
        System.out.println("\n[TEST 5] Rollback on failure:");
        check(throwsDataAccess(() -> repo.transfer(1003, 1001, 999999.0)), "Insufficient balance should fail");
        check(throwsDataAccess(() -> repo.transfer(1001, 9999, 100.0)), "Missing destination should fail");
        check(throwsDataAccess(() -> repo.transfer(1001, 1003, -5.0)), "Negative amount should fail");
        b1 = repo.findById(1001).getBalance();
        b3 = repo.findById(1003).getBalance();
        System.out.println("  Balances unchanged: 1001 = Rs. " + b1 + ", 1003 = Rs. " + b3);
        check(b1 == 60000.0 && b3 == 25000.0, "Failed transfers must not change balances");
        System.out.println("  -> PASSED");

        // TEST 6: nextAccountNumber & PIN round-trip
        System.out.println("\n[TEST 6] nextAccountNumber & PIN mapping:");
        int next = repo.nextAccountNumber();
        System.out.println("  Next account number: " + next + " (Expected: 1004)");
        check(next == 1004, "Next account number should be MAX + 1");
        check(repo.findById(1001).verifyPin(1234), "PIN should round-trip through the DB");
        System.out.println("  -> PASSED");

        // Clean up
        provider.shutdown();
        if (dbFile.exists()) dbFile.delete();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ALL ACTIVITY 23 JDBC ACCOUNT REPOSITORY TESTS PASSED!");
        System.out.println("=".repeat(60));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean throwsDataAccess(Runnable action) {
        try {
            action.run();
            return false;
        } catch (DataAccessException e) {
            System.out.println("  Rejected: " + e.getMessage());
            return true;
        }
    }
}
