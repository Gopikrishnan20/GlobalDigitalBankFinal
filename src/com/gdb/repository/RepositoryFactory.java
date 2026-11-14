package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.HikariConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;

import java.io.*;
import java.util.Properties;

public class RepositoryFactory {

    private static AccountRepository accountRepositoryInstance;
    private static TransactionRepository transactionRepositoryInstance;
    private static ConnectionProvider connectionProviderInstance;

    public static Properties loadPersistenceProperties() {
        Properties props = new Properties();
        String[] paths = {
            "config/persistence.properties",
            "src/main/resources/config/persistence.properties",
            "bin/config/persistence.properties"
        };

        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    return props;
                } catch (IOException ignored) {}
            }
        }
        return props;
    }

    public static String getPersistenceMode() {
        return loadPersistenceProperties().getProperty("persistence.mode", "memory").trim().toLowerCase();
    }

    public static synchronized ConnectionProvider getConnectionProvider() {
        if (connectionProviderInstance == null) {
            Properties props = loadPersistenceProperties();
            boolean poolEnabled = Boolean.parseBoolean(props.getProperty("persistence.pool.enabled", "false").trim());
            if (poolEnabled) {
                connectionProviderInstance = new HikariConnectionProvider(props);
            } else {
                String url = props.getProperty("persistence.db.url", "jdbc:sqlite:gdb.db");
                String driver = props.getProperty("persistence.db.driver", "org.sqlite.JDBC");
                connectionProviderInstance = new JdbcConnectionProvider(url, driver);
            }
            SchemaInitializer.initialize(connectionProviderInstance);
        }
        return connectionProviderInstance;
    }

    public static synchronized AccountRepository getAccountRepository() {
        if (accountRepositoryInstance == null) {
            String mode = getPersistenceMode();
            if ("memory".equals(mode)) {
                accountRepositoryInstance = new InMemoryAccountRepository();
            } else if ("jdbc".equals(mode)) {
                accountRepositoryInstance = new JdbcAccountRepository(getConnectionProvider());
            } else if ("file".equals(mode)) {
                Properties props = loadPersistenceProperties();
                accountRepositoryInstance = new FileAccountRepository(
                    props.getProperty("persistence.file.account.path", "data/accounts.csv"),
                    backupEnabled(props), maxBackups(props));
            } else {
                accountRepositoryInstance = new InMemoryAccountRepository();
            }
        }
        return accountRepositoryInstance;
    }

    public static synchronized TransactionRepository getTransactionRepository() {
        if (transactionRepositoryInstance == null) {
            String mode = getPersistenceMode();
            if ("memory".equals(mode)) {
                transactionRepositoryInstance = new InMemoryTransactionRepository();
            } else if ("jdbc".equals(mode)) {
                transactionRepositoryInstance = new JdbcTransactionRepository(getConnectionProvider());
            } else if ("file".equals(mode)) {
                Properties props = loadPersistenceProperties();
                transactionRepositoryInstance = new FileTransactionRepository(
                    props.getProperty("persistence.file.transaction.path", "data/transactions.csv"),
                    (FileAccountRepository) getAccountRepository(),
                    backupEnabled(props), maxBackups(props));
            } else {
                transactionRepositoryInstance = new InMemoryTransactionRepository();
            }
        }
        return transactionRepositoryInstance;
    }

    private static boolean backupEnabled(Properties props) {
        return Boolean.parseBoolean(props.getProperty("persistence.file.backup.enabled", "true").trim());
    }

    private static int maxBackups(Properties props) {
        return Integer.parseInt(props.getProperty("persistence.file.backup.max", "5").trim());
    }

    public static synchronized void reset() {
        if (connectionProviderInstance != null) {
            connectionProviderInstance.shutdown();
            connectionProviderInstance = null;
        }
        accountRepositoryInstance = null;
        transactionRepositoryInstance = null;
    }
}
