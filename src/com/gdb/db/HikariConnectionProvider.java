package com.gdb.db;

import com.gdb.exceptions.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ConnectionProvider backed by a HikariCP connection pool.
 * Connections are created once and reused; closing a borrowed connection returns it to the pool.
 */
public class HikariConnectionProvider implements ConnectionProvider {

    private final HikariDataSource dataSource;

    public HikariConnectionProvider(Properties config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getProperty("persistence.db.url", "jdbc:sqlite:gdb.db"));
        hikariConfig.setDriverClassName(config.getProperty("persistence.db.driver", "org.sqlite.JDBC"));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(config.getProperty("persistence.pool.size", "10").trim()));
        hikariConfig.setConnectionTimeout(Long.parseLong(config.getProperty("persistence.pool.timeout", "30000").trim()));
        hikariConfig.setIdleTimeout(Long.parseLong(config.getProperty("persistence.pool.idleTimeout", "600000").trim()));
        try {
            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (RuntimeException e) {
            throw new DataAccessException("Failed to initialise connection pool: " + e.getMessage(), e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public String getProviderName() {
        return "HikariConnectionProvider [Pool size: " + dataSource.getMaximumPoolSize() + "]";
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
