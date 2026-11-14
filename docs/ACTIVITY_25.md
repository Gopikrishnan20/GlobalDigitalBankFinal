# ACTIVITY 25: Connection Pooling with HikariCP

## Objective

Reuse database connections through a HikariCP pool instead of opening a new
physical connection for every repository call, and switch between pooled and
direct connections from configuration only.

## Architecture

```text
RepositoryFactory.getConnectionProvider()
        │  persistence.pool.enabled=true                persistence.pool.enabled=false
        ├──► HikariConnectionProvider ──► HikariDataSource (pool) ──► SQLite
        └──► JdbcConnectionProvider   ──► DriverManager (new connection each call)

Repository code is unchanged: it calls provider.getConnection() and closes the
connection in try-with-resources. With a pool, close() returns it to the pool.
```

## Files

| File | Description |
|---|---|
| `lib/HikariCP-5.0.1.jar` | HikariCP connection pool library |
| `src/com/gdb/db/HikariConnectionProvider.java` | `ConnectionProvider` adapter over `HikariDataSource` |
| `src/com/gdb/repository/RepositoryFactory.java` | Chooses the pooled or direct provider from `persistence.pool.enabled` |
| `src/main/resources/config/persistence.properties` | `persistence.pool.enabled`, `persistence.pool.size`, `persistence.pool.timeout` |
| `src/com/gdb/tests/TestConnectionPooling.java` | Pool, borrow/return, benchmark, repository and shutdown tests |

## Configuration

| Key | Default | Meaning |
|---|---|---|
| `persistence.pool.enabled` | `false` | Use HikariCP when `true` |
| `persistence.pool.size` | `10` | Maximum connections in the pool |
| `persistence.pool.timeout` | `30000` | Milliseconds to wait for a free connection |
| `persistence.pool.idleTimeout` | `600000` | Milliseconds before an idle connection is retired |

## Tests (`TestConnectionPooling`, uses `test_pool_gdb.db`)

| Test | Checks |
|---|---|
| 1 | Pool starts with the configured size and hands out a valid connection |
| 2 | One active connection while borrowed, zero after `close()` |
| 3 | 100 connect/query cycles: pooled is faster than direct |
| 4 | Account save and atomic transfer through pooled repositories, no leaked connections |
| 5 | `shutdown()` closes the pool |

## How to Compile & Run

```bash
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src/com -name "*.java")
java -cp "bin;lib/*;src/main/resources" com.gdb.tests.TestConnectionPooling
```
