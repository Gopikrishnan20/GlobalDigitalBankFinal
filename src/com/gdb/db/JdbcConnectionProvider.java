package com.gdb.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Direct JDBC Connection Provider using DriverManager.
 */
public class JdbcConnectionProvider implements ConnectionProvider {

    private final String url;

    public JdbcConnectionProvider(String url, String driver) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(driver, "driver must not be null");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("JDBC driver class not found: " + driver, exception);
        }
    }

    public JdbcConnectionProvider(String url) {
        this(url, "org.sqlite.JDBC");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String getProviderName() {
        return "JdbcConnectionProvider [" + url + "]";
    }

    public String getUrl() {
        return url;
    }
}
