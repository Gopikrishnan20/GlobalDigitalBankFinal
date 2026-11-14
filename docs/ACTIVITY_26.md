# ACTIVITY 26: File Repository – CSV, Atomic Writes, and Backups

## Objective

Store accounts and transactions in CSV files when no database is available,
without ever leaving a half-written file, and keep rotating backups of each file.

## Architecture

```text
RepositoryFactory (persistence.mode=file)
        ├──► FileAccountRepository     ──► data/accounts.csv
        └──► FileTransactionRepository ──► data/transactions.csv
                     │ (uses FileAccountRepository for transfer balances)
                     ▼
every write:  BackupManager.backup(file, max)  →  copy to file.bak.<yyyyMMdd_HHmmss_SSS_nnnn>, delete oldest
              AtomicFileWriter.write(file, lines)  →  write file.tmp, then ATOMIC_MOVE over file
mapping:      CsvMapper  IAccount ⇄ CSV row,  Transaction ⇄ CSV row  (quotes fields containing , " or newlines)
```

## Files

| File | Description |
|---|---|
| `src/com/gdb/file/AtomicFileWriter.java` | Writes to `.tmp` then renames with `ATOMIC_MOVE` (falls back to `REPLACE_EXISTING`) |
| `src/com/gdb/file/CsvMapper.java` | Data Mapper for accounts and transactions, with CSV quoting |
| `src/com/gdb/file/BackupManager.java` | Timestamped backups, keeps the newest `maxBackups` |
| `src/com/gdb/repository/FileAccountRepository.java` | CSV-backed `AccountRepository` with an in-memory cache |
| `src/com/gdb/repository/FileTransactionRepository.java` | CSV-backed `TransactionRepository`; transfer updates `accounts.csv` and appends a TRANSFER row |
| `src/com/gdb/repository/RepositoryFactory.java` | New `file` mode |
| `src/com/gdb/domain/Account.java` | `restoreState(balance, openingDate)` used when rebuilding accounts from storage |
| `src/main/resources/config/persistence.properties` | `persistence.file.*` settings |
| `src/com/gdb/tests/TestFileRepository.java` | Test suite (uses a throw-away `test_data/` folder) |

## Transfer in file mode

1. Validate: positive amount, different accounts, both exist and are Active, sender has the funds.
2. Write both new balances to `accounts.csv` in one atomic write.
3. Append the TRANSFER row to `transactions.csv`.
4. If step 3 fails, write the old balances back and report "Transfer rolled back".

## Tests (`TestFileRepository`)

| Test | Checks |
|---|---|
| 1 | Save and re-read an account; comma in the name and the PIN survive |
| 2 | Update and delete are written to disk; next number is MAX + 1; no `.tmp` left |
| 3 | Transactions saved and queried by account; comma in the description survives |
| 4 | Transfer 5000: 60000/40000 → 55000/45000 on disk; failed transfers change nothing |
| 5 | Only the newest 3 backups are kept, named with a timestamp |
| 6 | `AtomicFileWriter` leaves no temporary file |

## How to Compile & Run

```bash
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src/com -name "*.java")
java -cp "bin;lib/*;src/main/resources" com.gdb.tests.TestFileRepository
```

Set `persistence.mode=file` in `persistence.properties` to run the whole app on CSV files.
