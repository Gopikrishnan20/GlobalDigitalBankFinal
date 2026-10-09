package com.gdb.repository;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Factory creating and supplying repository instances based on persistence.properties configuration.
 */
public class RepositoryFactory {

    private static AccountRepository accountRepositoryInstance;
    private static TransactionRepository transactionRepositoryInstance;

    public static String getPersistenceMode() {
        Properties properties = new Properties();
        try (InputStream input = openPersistenceProperties()) {
            if (input == null) {
                return "memory";
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read config/persistence.properties", exception);
        }

        String mode = properties.getProperty("persistence.mode", "memory").trim();
        return mode.isEmpty() ? "memory" : mode.toLowerCase(Locale.ROOT);
    }

    public static synchronized AccountRepository getAccountRepository() {
        String mode = getPersistenceMode();
        if ("jdbc".equals(mode)) {
            throw new UnsupportedOperationException(
                    "JDBC repository not implemented yet - coming in Activity 22");
        }
        if ("file".equals(mode)) {
            throw new UnsupportedOperationException("File repository not implemented yet");
        }
        if (accountRepositoryInstance == null) {
            accountRepositoryInstance = new InMemoryAccountRepository();
        }
        return accountRepositoryInstance;
    }

    public static synchronized TransactionRepository getTransactionRepository() {
        String mode = getPersistenceMode();
        if ("jdbc".equals(mode)) {
            throw new UnsupportedOperationException(
                    "JDBC repository not implemented yet - coming in Activity 22");
        }
        if ("file".equals(mode)) {
            throw new UnsupportedOperationException("File repository not implemented yet");
        }
        if (transactionRepositoryInstance == null) {
            transactionRepositoryInstance = new InMemoryTransactionRepository();
        }
        return transactionRepositoryInstance;
    }

    private static InputStream openPersistenceProperties() throws IOException {
        InputStream resource = RepositoryFactory.class.getClassLoader()
                .getResourceAsStream("config/persistence.properties");
        if (resource != null) {
            return resource;
        }

        File configuredFile = new File("config/persistence.properties");
        if (!configuredFile.isFile()) {
            configuredFile = new File("src/main/resources/config/persistence.properties");
        }
        return configuredFile.isFile() ? new FileInputStream(configuredFile) : null;
    }
}
