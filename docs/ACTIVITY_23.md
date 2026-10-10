# ACTIVITY 23: JdbcAccountRepository (CRUD + Atomic Transfer)

## Objective

Deliver relational persistence for accounts using JDBC `PreparedStatement`s,
map `ResultSet` rows back to domain objects, and perform multi-step balance
transfers inside a single ACID transaction with rollback safety.

## Architecture

```text
RepositoryFactory (persistence.mode=jdbc)
        │
        └──► JdbcAccountRepository ──► ConnectionProvider ──► SQLite (accounts)
                    │
                    ├── save / findById / findAll / update / delete / exists / nextAccountNumber
                    └── transfer(from, to, amount)
                           setAutoCommit(false)
                           ├─ read + validate source (exists, Active, enough balance)
                           ├─ read + validate destination (exists, Active)
                           ├─ UPDATE balance = balance - amount   (source)
                           ├─ UPDATE balance = balance + amount   (destination)
                           └─ commit()   ── any failure ──► rollback()
```

All `SQLException`s are wrapped in the unchecked `DataAccessException`, so
service code does not depend on `java.sql`.

## Files

| File | Description |
|---|---|
| `src/com/gdb/exceptions/DataAccessException.java` | Unchecked exception wrapping SQL errors |
| `src/com/gdb/repository/AccountRepository.java` | Adds `transfer(int, int, double)` to the contract |
| `src/com/gdb/repository/JdbcAccountRepository.java` | JDBC CRUD, row mapping, atomic transfer |
| `src/com/gdb/repository/InMemoryAccountRepository.java` | In-memory `transfer` implementation |
| `src/com/gdb/repository/RepositoryFactory.java` | Supplies `JdbcAccountRepository` in JDBC mode |
| `src/com/gdb/domain/Account.java` | Adds `getAge()`, `getPin()`, `getStatus()` for persistence |
| `src/com/gdb/tests/TestJdbcAccountRepository.java` | CRUD, transfer commit, rollback and mapping tests |

## Column Mapping

| Column | Source |
|---|---|
| `account_number`, `account_holder_name`, `balance`, `account_type`, `opening_date`, `tenure_years` | `IAccount` getters |
| `age`, `pin` | `Account.getAge()`, `Account.getPin()` (`NULL` when no PIN) |
| `status` | `"Active"` / `"Closed"` from `isActive()` |

## How to Compile & Run

### Windows (PowerShell)
```powershell
New-Item bin -ItemType Directory -Force | Out-Null
Copy-Item src\main\resources\* bin -Recurse -Force
javac -encoding UTF-8 -cp "lib/*;." -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp "lib/*;bin" com.gdb.tests.TestJdbcAccountRepository
```

### Linux & macOS
```bash
mkdir -p bin && cp -r src/main/resources/* bin/
javac -encoding UTF-8 -cp "lib/*:." -d bin $(find src -name "*.java")
java -cp "lib/*:bin" com.gdb.tests.TestJdbcAccountRepository
```

## Tests

1. Save & FindById
2. Update & FindAll
3. Delete & Exists
4. Atomic transfer commits debit and credit together
5. Insufficient balance, missing account and negative amount are rejected with balances unchanged
6. `nextAccountNumber()` returns `MAX + 1`; PIN survives a DB round-trip

## Exercises

1. Inject a failure between the debit and credit `UPDATE`s and confirm `rollback()` restores the source balance.
2. Add a `findByType(String)` query using a `PreparedStatement`.
3. Explain why `balance = balance + ?` is safer than writing a balance computed in Java.
