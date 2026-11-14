# ACTIVITY 24: JdbcTransactionRepository & Data Migration

## Objective

Persist transaction records in SQLite, run multi-account transfers as one ACID
transaction, and migrate legacy in-memory data and command logs into the database.

## Architecture

```text
RepositoryFactory (persistence.mode=jdbc)
        ├──► JdbcAccountRepository      (Activity 23)
        └──► JdbcTransactionRepository ──► ConnectionProvider ──► SQLite (transactions, accounts)
                    ├── save / findByAccount / findAll / clear
                    └── transfer(from, to, amount)        one connection, setAutoCommit(false)
                           ├─ check both accounts exist, are Active, sender has funds
                           ├─ UPDATE balance = balance - amount   (sender)
                           ├─ UPDATE balance = balance + amount   (receiver)
                           ├─ INSERT TRANSFER audit row          (same connection)
                           └─ commit()  ── any failure ──► rollback()

DataMigrator
   migrate(LogDestination, TransactionRepository)                 command log → transactions table
   migrate(AccountRepository, AccountRepository,
           TransactionRepository, TransactionRepository)          accounts first, then transactions
```

## Files

| File | Description |
|---|---|
| `src/com/gdb/repository/JdbcTransactionRepository.java` | JDBC transaction repository with atomic transfer |
| `src/com/gdb/db/DataMigrator.java` | Migrates command logs and repositories into another backend |
| `src/com/gdb/repository/RepositoryFactory.java` | Returns `JdbcTransactionRepository` in `jdbc` mode |
| `src/com/gdb/service/AccountService.java` | New `setPin(accountNumber, pin)` that saves the PIN through the repository |
| `src/com/gdb/ui/AccountUI.java` | Uses `service.setPin` so the PIN is persisted in JDBC mode |
| `src/com/gdb/tests/TestJdbcTransactionRepository.java` | Test suite for queries, transfer, rollback and migration |
| `src/com/gdb/tests/TestAccountService.java`, `TestAccountUI.java` | Use in-memory repositories so they stay independent of `gdb.db` |

## Tests (`TestJdbcTransactionRepository`, uses `test_txn_gdb.db`)

| Test | Checks |
|---|---|
| 1 | Save 2 records for #1001, `findByAccount` returns them in order |
| 2 | Transfer Rs. 10000: 50000/30000 → 40000/40000, TRANSFER row visible to the receiver |
| 3 | Insufficient balance, missing account, negative amount: rejected, balances and audit rows unchanged |
| 4 | Migrate 3 accounts and 5 records from memory repositories into SQLite |
| 5 | Migrate 3 executed commands from a `MemoryLogDestination` |

## How to Compile & Run

```bash
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src/com -name "*.java")
java -cp "bin;lib/*;src/main/resources" com.gdb.tests.TestJdbcTransactionRepository
```
