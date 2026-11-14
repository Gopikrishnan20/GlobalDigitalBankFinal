package com.gdb.tests;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.HikariConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.repository.JdbcAccountRepository;
import com.gdb.repository.JdbcTransactionRepository;
import com.zaxxer.hikari.HikariPoolMXBean;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class TestConnectionPooling {
    private static final int CYCLES = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 25 — JDBC CONNECTION POOLING (HIKARICP)");
        System.out.println("=".repeat(60));

        Properties props = new Properties();
        props.setProperty("persistence.db.url", "jdbc:sqlite:test_pool_gdb.db");
        props.setProperty("persistence.db.driver", "org.sqlite.JDBC");
        props.setProperty("persistence.pool.enabled", "true");
        props.setProperty("persistence.pool.size", "5");
        props.setProperty("persistence.pool.timeout", "10000");

        File dbFile = new File("test_pool_gdb.db");
        if (dbFile.exists()) dbFile.delete();

        System.out.println("[TEST 1] HikariCP Connection Pool Initialization:");
        HikariConnectionProvider pooled = new HikariConnectionProvider(props);
        SchemaInitializer.initialize(pooled);
        System.out.println("  Pool Name: " + pooled.getDataSource().getPoolName());
        System.out.println("  Max Pool Size: " + pooled.getDataSource().getMaximumPoolSize());
        check(pooled.getDataSource().getMaximumPoolSize() == 5, "Pool size should come from configuration");
        try (Connection conn = pooled.getConnection()) {
            System.out.println("  Connection acquired: " + conn.isValid(1));
            check(conn.isValid(1), "Pooled connection should be valid");
        }
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 2] Borrow & Return:");
        HikariPoolMXBean pool = pooled.getDataSource().getHikariPoolMXBean();
        try (Connection conn = pooled.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS result")) {
            check(rs.next() && rs.getInt("result") == 1, "Query over pooled connection should work");
            System.out.println("  While borrowed -> active connections: " + pool.getActiveConnections());
            check(pool.getActiveConnections() == 1, "One connection should be in use");
        }
        System.out.println("  After close()  -> active connections: " + pool.getActiveConnections()
                + ", idle connections: " + pool.getIdleConnections());
        check(pool.getActiveConnections() == 0, "close() should return the connection to the pool");
        check(pool.getTotalConnections() <= 5, "Pool must never exceed its maximum size");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 3] Latency Benchmark (" + CYCLES + " sequential connection cycles):");
        ConnectionProvider direct = new JdbcConnectionProvider("jdbc:sqlite:test_pool_gdb.db");
        runCycles(direct, 10);   // warm-up so class loading is not counted
        runCycles(pooled, 10);
        long directMs = runCycles(direct, CYCLES);
        long pooledMs = runCycles(pooled, CYCLES);
        System.out.printf("  Unpooled Direct Connections: %d ms (avg %.2f ms/op)%n", directMs, (double) directMs / CYCLES);
        System.out.printf("  HikariCP Pooled Connections:  %d ms (avg %.2f ms/op)%n", pooledMs, (double) pooledMs / CYCLES);
        System.out.printf("  Speedup Factor: ~%.1fx faster%n", (double) directMs / Math.max(1, pooledMs));
        check(pooledMs <= directMs, "Pooled connections should not be slower than direct connections");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 4] Repository Operations over HikariCP:");
        JdbcAccountRepository accounts = new JdbcAccountRepository(pooled);
        JdbcTransactionRepository transactions = new JdbcTransactionRepository(pooled);
        accounts.save(AccountFactory.createAccount("SAVINGS", 1001, "Rajesh Sharma", 30, 50000.0));
        accounts.save(AccountFactory.createAccount("SAVINGS", 1002, "Priya Patel", 28, 30000.0));
        System.out.println("  Saved Accounts #1001 and #1002 via pooled repository.");
        transactions.transfer(1001, 1002, 10000.0);
        IAccount sender = accounts.findById(1001);
        System.out.println("  Retrieved Account #1001 after transfer: Balance Rs. " + sender.getBalance());
        check(sender.getBalance() == 40000.0 && accounts.findById(1002).getBalance() == 40000.0,
                "Atomic transfer should work over pooled connections");
        check(pool.getActiveConnections() == 0, "Repositories must return every connection (no leaks)");
        System.out.println("  Active connections after repository calls: " + pool.getActiveConnections() + " (no leaks)");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 5] Shutdown:");
        pooled.shutdown();
        check(pooled.getDataSource().isClosed(), "Pool should be closed after shutdown()");
        System.out.println("  Pool closed: " + pooled.getDataSource().isClosed());
        System.out.println("  -> PASSED");

        if (dbFile.exists()) dbFile.delete();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ALL ACTIVITY 25 CONNECTION POOLING TESTS PASSED!");
        System.out.println("=".repeat(60));
    }

    private static long runCycles(ConnectionProvider provider, int cycles) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < cycles; i++) {
            try (Connection conn = provider.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts")) {
                rs.next();
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
