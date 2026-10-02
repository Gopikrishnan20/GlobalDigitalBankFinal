# ACTIVITY 20: AccountUI (Interactive Console Application)

## Objective
Add `AccountUI`, a menu-driven console interface on top of `AccountService`. The UI only reads input, calls the service, and prints results — all business rules stay in the service and domain layers.

---

## Architecture

```text
Main ──► AccountUI ──► AccountService ──► AccountFactory / TransferService
                                     └──► TransactionLogger ──► LogDestination (FILE)
```

---

## Files
| File | Description |
|---|---|
| `src/com/gdb/ui/AccountUI.java` | Interactive menu-driven console UI |
| `src/com/gdb/tests/TestAccountUI.java` | Drives the UI with scripted input and verifies the output |
| `src/com/gdb/Main.java` | Wires `FileLogDestination` → `TransactionLogger` → `AccountService` → `AccountUI` |

---

## Menu
| Option | Action |
|---|---|
| 1 | Open Account (type, name, age, initial balance, PIN) |
| 2 | Deposit |
| 3 | Withdraw (requires PIN) |
| 4 | Transfer (requires PIN) |
| 5 | View Account |
| 6 | List All Accounts |
| 7 | Transaction History |
| 8 | Close Account (requires PIN) |
| 0 | Exit |

Errors (wrong PIN, missing account, invalid numbers, unknown options) are printed and the menu continues.

---

## Testability
`AccountUI` has a second constructor `AccountUI(AccountService, Scanner, PrintStream)`. `TestAccountUI` feeds a scripted `Scanner` and captures output in a `ByteArrayOutputStream`, so the full menu flow is tested without typing.

---

## How to Compile & Run

```bash
mkdir -p bin && cp -r src/main/resources/config bin/
javac -encoding UTF-8 -d bin $(find src/com -name "*.java")
java -cp bin com.gdb.tests.TestAccountUI
java -cp bin com.gdb.Main
```
