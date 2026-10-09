package com.gdb.db;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Initializes the database schema idempotently by executing schema.sql DDL statements.
 */
public class SchemaInitializer {

    public static void initialize(ConnectionProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        String[] statements = readSchemaSql().split(";");
        try (Connection connection = provider.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialize the database schema", exception);
        }
    }

    public static String readSchemaSql() {
        InputStream resource = SchemaInitializer.class.getClassLoader()
                .getResourceAsStream("schema.sql");
        if (resource == null) {
            File[] candidates = {
                new File("src/main/resources/schema.sql"),
                new File("bin/schema.sql"),
                new File("schema.sql")
            };
            for (File candidate : candidates) {
                if (candidate.isFile()) {
                    try {
                        resource = new FileInputStream(candidate);
                        break;
                    } catch (FileNotFoundException exception) {
                        throw new IllegalStateException(
                                "Could not read schema file: " + candidate.getPath(), exception);
                    }
                }
            }
        }
        if (resource == null) {
            throw new IllegalStateException("Database schema resource schema.sql was not found");
        }
        try (InputStream input = resource;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            return output.toString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read database schema resource schema.sql", exception);
        }
    }
}
