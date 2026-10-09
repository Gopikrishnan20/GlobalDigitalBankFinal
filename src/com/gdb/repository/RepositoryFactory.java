package com.gdb.repository;

import com.gdb.db.ConnectionProvider;
import com.gdb.db.JdbcConnectionProvider;
import com.gdb.db.SchemaInitializer;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Factory creating and supplying repository instances based on persistence.properties configuration.
 */
public class RepositoryFactory {

    private static AccountRepository accountRepositoryInstance;
    private static TransactionRepository transactionRepositoryInstance;
    private static ConnectionProvider connectionProviderInstance;

    public static Properties loadPersistenceProperties() {
        Properties props = new Properties();
        try (InputStream resource = RepositoryFactory.class.getClassLoader()
                .getResourceAsStream("config/persistence.properties")) {
            if (resource != null) {
                props.load(resource);
                return props;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read config/persistence.properties", exception);
        }

        String[] paths = {
            "config/persistence.properties",
            "src/main/resources/config/persistence.properties",
            "bin/config/persistence.properties"
        };

        for (String p : paths) {
            File f = new File(p);
            if (f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    props.load(in);
                    return props;
                } catch (IOException exception) {
                    throw new IllegalStateException("Could not read " + f.getPath(), exception);
                }
            }
        }
        return props;
    }

    public static String getPersistenceMode() {
        String mode = loadPersistenceProperties().getProperty("persistence.mode", "memory").trim();
        return mode.isEmpty() ? "memory" : mode.toLowerCase(Locale.ROOT);
    }

    public static synchronized ConnectionProvider getConnectionProvider() {
        if (connectionProviderInstance == null) {
            Properties properties = loadPersistenceProperties();
            String url = properties.getProperty("persistence.db.url", "jdbc:sqlite:gdb.db").trim();
            String driver = properties.getProperty("persistence.db.driver", "org.sqlite.JDBC").trim();
            ConnectionProvider provider = new JdbcConnectionProvider(url, driver);
            try {
                SchemaInitializer.initialize(provider);
                connectionProviderInstance = provider;
            } catch (RuntimeException exception) {
                provider.shutdown();
                throw exception;
            }
        }
        return connectionProviderInstance;
    }

    public static synchronized AccountRepository getAccountRepository() {
        if (accountRepositoryInstance == null) {
            String mode = getPersistenceMode();
            if ("memory".equals(mode)) {
                accountRepositoryInstance = new InMemoryAccountRepository();
            } else if ("jdbc".equals(mode)) {
                // Connection provider initialized
                getConnectionProvider();
                // Placeholder for Activity 23 JdbcAccountRepository
                throw new UnsupportedOperationException("JdbcAccountRepository will be implemented in Activity 23. Use memory mode or TestJdbcConnection for Activity 22.");
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
                getConnectionProvider();
                // Placeholder for Activity 24 JdbcTransactionRepository
                throw new UnsupportedOperationException("JdbcTransactionRepository will be implemented in Activity 24. Use memory mode or TestJdbcConnection for Activity 22.");
            } else {
                transactionRepositoryInstance = new InMemoryTransactionRepository();
            }
        }
        return transactionRepositoryInstance;
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
