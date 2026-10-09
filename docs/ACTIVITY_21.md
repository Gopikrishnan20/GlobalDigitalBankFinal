# ACTIVITY 21: Repository Pattern (Interfaces + In-Memory + Factory)

## Objective

Decouple the account service from storage details by defining account and
transaction repository contracts, providing in-memory implementations, and
selecting those implementations through `persistence.properties`.

## Architecture

```text
AccountUI ──► AccountService ──► AccountRepository ──► InMemoryAccountRepository
                            └──► TransactionRepository ► InMemoryTransactionRepository
                                      ▲
                               RepositoryFactory
                                      ▲
                         config/persistence.properties
```

The service depends on repository interfaces. The factory currently supports
`memory`; `jdbc` and `file` are reserved for future activities.

## Files

| File | Description |
|---|---|
| `src/com/gdb/repository/AccountRepository.java` | Account persistence contract |
| `src/com/gdb/repository/TransactionRepository.java` | Transaction persistence contract |
| `src/com/gdb/repository/InMemoryAccountRepository.java` | Account storage backed by a map, with account-number generation |
| `src/com/gdb/repository/InMemoryTransactionRepository.java` | Transaction storage backed by a list, including account filtering |
| `src/com/gdb/repository/RepositoryFactory.java` | Loads the persistence mode and supplies repository instances |
| `src/main/resources/config/persistence.properties` | Declares the active persistence mode |
| `src/com/gdb/service/AccountService.java` | Uses repository interfaces to load and persist accounts and transactions |
| `src/com/gdb/tests/TestRepositoryInMemory.java` | Executable CRUD, filtering, factory, and service-integration checks |

## Configuration

`src/main/resources/config/persistence.properties`:

```properties
persistence.mode=memory
```

When packaged or run from the project root, the factory checks the classpath
resource and supported project-relative configuration locations. If no
configuration file exists, it defaults to `memory`. Unsupported `jdbc` and
`file` modes fail explicitly.

## How to Compile and Run

### Windows (PowerShell)

```powershell
New-Item bin -ItemType Directory -Force | Out-Null
Copy-Item src\main\resources\config bin -Recurse -Force
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp bin com.gdb.tests.TestRepositoryInMemory
```

### Linux and macOS (Terminal / Bash)

```bash
mkdir -p bin && cp -r src/main/resources/config bin/
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin com.gdb.tests.TestRepositoryInMemory
```

The test driver throws an assertion error on a failed check and reports the
CRUD, account-number generation, transaction filtering, factory singleton, and
service-integration checks as they pass.

## Exercises

1. Add a file-backed implementation without changing `AccountService`.
2. Add a JDBC implementation and route `persistence.mode=jdbc` to it.
3. Add repository tests for duplicate account numbers and empty transaction
   histories.
