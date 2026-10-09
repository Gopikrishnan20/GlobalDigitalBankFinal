# ACTIVITY 22: JDBC Foundation (Connection, Schema, Provider)

## Objective

Introduce SQLite JDBC connection acquisition behind a `ConnectionProvider`,
create the initial relational schema idempotently, and configure
`RepositoryFactory` to provide the JDBC connection foundation.

## Architecture

```text
RepositoryFactory ──► JdbcConnectionProvider ──► DriverManager / SQLite
        │
        └───────────► SchemaInitializer ──► schema.sql
```

The database connection layer is ready, but JDBC account and transaction
repositories are intentionally deferred to later activities. With JDBC mode
configured, requesting those repositories still reports that they are not
implemented. Select `persistence.mode=memory` to run the service/UI activities.

## Files

| File | Description |
|---|---|
| `lib/sqlite-jdbc-3.44.1.0.jar` and SLF4J runtime jars | SQLite JDBC driver and logging dependencies |
| `src/com/gdb/db/ConnectionProvider.java` | Connection acquisition and lifecycle contract |
| `src/com/gdb/db/JdbcConnectionProvider.java` | Loads the configured JDBC driver and opens connections |
| `src/main/resources/schema.sql` | Idempotent `accounts` and `transactions` table definitions |
| `src/com/gdb/db/SchemaInitializer.java` | Loads and executes schema DDL statements |
| `src/main/resources/config/persistence.properties` | JDBC mode, URL, and driver configuration |
| `src/com/gdb/repository/RepositoryFactory.java` | Lazily initializes the provider and schema |
| `src/com/gdb/tests/TestJdbcConnection.java` | Checks `SELECT 1`, factory configuration, and table initialization |

## Configuration

```properties
persistence.mode=jdbc
persistence.db.url=jdbc:sqlite:gdb.db
persistence.db.driver=org.sqlite.JDBC
```

The schema initializer locates `schema.sql` on the classpath or in the standard
project resource paths. Schema creation uses `CREATE TABLE IF NOT EXISTS`, so
initialization can be safely repeated.

## How to Compile and Run

The SQLite JDBC and SLF4J runtime jars are provided in `lib`.

### Windows (PowerShell)

```powershell
New-Item bin -ItemType Directory -Force | Out-Null
Copy-Item src\main\resources\* bin -Recurse -Force
javac -encoding UTF-8 -cp "lib/*;." -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp "lib/*;bin" com.gdb.tests.TestJdbcConnection
```

### Linux and macOS (Terminal / Bash)

```bash
mkdir -p bin && cp -r src/main/resources/* bin/
javac -encoding UTF-8 -cp "lib/*:." -d bin $(find src -name "*.java")
java -cp "lib/*:bin" com.gdb.tests.TestJdbcConnection
```

## Exercises

1. Add a pooled connection provider while preserving the provider interface.
2. Extend the schema to support new account types without changing domain code.
3. Implement the JDBC account repository in the next activity.
