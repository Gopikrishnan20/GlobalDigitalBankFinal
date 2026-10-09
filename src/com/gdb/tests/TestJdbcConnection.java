package com.gdb.tests;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;
import com.gdb.repository.RepositoryFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class TestJdbcConnection {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 22 — JDBC FOUNDATION (CONNECTION & SCHEMA)");
        System.out.println("=".repeat(60));

        String testDbUrl = "jdbc:sqlite:test_gdb.db";
        ConnectionProvider provider = new JdbcConnectionProvider(testDbUrl);

        try {
            try (Connection connection = provider.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT 1")) {
                check(result.next() && result.getInt(1) == 1, "JDBC connection executes SELECT 1");
            }

            SchemaInitializer.initialize(provider);
            SchemaInitializer.initialize(provider);
            Set<String> tableNames = new HashSet<>();
            try (Connection connection = provider.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table'")) {
                while (result.next()) {
                    tableNames.add(result.getString("name"));
                }
            }
            check(tableNames.contains("accounts"), "accounts table exists");
            check(tableNames.contains("transactions"), "transactions table exists");
            check(provider.getProviderName().contains(testDbUrl), "provider reports its configured URL");
            ConnectionProvider factoryProvider = RepositoryFactory.getConnectionProvider();
            String configuredUrl = RepositoryFactory.loadPersistenceProperties()
                    .getProperty("persistence.db.url", "jdbc:sqlite:gdb.db").trim();
            check(factoryProvider.getProviderName().contains(configuredUrl),
                    "factory provider uses persistence.properties");
            RepositoryFactory.reset();
            System.out.println("[PASS] JDBC connection lifecycle and SELECT 1");
            System.out.println("[PASS] Idempotent schema initialization, factory wiring, and table existence");
            System.out.println("Activity 22 JDBC tests complete.");
        } finally {
            provider.shutdown();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Failed: " + message);
        }
    }
}
