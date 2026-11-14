package com.gdb.tests;

import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.AtomicFileWriter;
import com.gdb.file.BackupManager;
import com.gdb.file.CsvMapper;
import com.gdb.repository.FileAccountRepository;
import com.gdb.repository.FileTransactionRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TestFileRepository {
    private static final String DIR = "test_data";
    private static final String ACC_PATH = DIR + "/accounts.csv";
    private static final String TXN_PATH = DIR + "/transactions.csv";

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 26 — FILE REPOSITORY (CSV + ATOMIC WRITES + BACKUP)");
        System.out.println("=".repeat(60));
        deleteDir();

        System.out.println("[TEST 1] Account Save & Read Back from CSV:");
        FileAccountRepository accounts = new FileAccountRepository(ACC_PATH, true, 3);
        IAccount rajesh = AccountFactory.createAccount("SAVINGS", 1001, "Sharma, Rajesh", 30, 50000.0);
        rajesh.setPin(1234);
        accounts.save(rajesh);
        System.out.println("  Saved: " + rajesh.getAccountInfo());
        IAccount found = new FileAccountRepository(ACC_PATH).findById(1001);   // fresh instance reads the file
        System.out.println("  Found: " + found.getAccountInfo());
        check(found.getBalance() == 50000.0, "Balance should survive the CSV round trip");
        check("Sharma, Rajesh".equals(found.getAccountHolderName()), "A comma inside a name should be escaped");
        check(found.verifyPin(1234), "PIN should survive the CSV round trip");
        check(Files.readAllLines(Paths.get(ACC_PATH)).get(0).equals(CsvMapper.ACCOUNT_HEADER), "File should start with the header");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 2] Multiple Accounts & Atomic Update:");
        accounts.save(AccountFactory.createAccount("CURRENT", 1002, "Priya Patel", 28, 40000.0));
        rajesh.deposit(10000.0);
        accounts.update(rajesh);
        FileAccountRepository reloaded = new FileAccountRepository(ACC_PATH);
        System.out.println("  Updated balance: Rs. " + reloaded.findById(1001).getBalance() + " (Expected: 60000.0)");
        System.out.println("  Total accounts in CSV: " + reloaded.findAll().size() + " (Expected: 2)");
        check(reloaded.findById(1001).getBalance() == 60000.0, "Update should be written to disk");
        check(reloaded.findAll().size() == 2, "Two accounts should be stored");
        check(reloaded.nextAccountNumber() == 1003, "Next number should be MAX + 1");
        check(!new File(ACC_PATH + ".tmp").exists(), "No temporary file should be left behind");
        accounts.save(AccountFactory.createAccount("SAVINGS", 1003, "Temp User", 22, 15000.0));
        accounts.delete(1003);
        check(!new FileAccountRepository(ACC_PATH).exists(1003), "Delete should be written to disk");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 3] Transaction Save & Query in CSV:");
        FileTransactionRepository txns = new FileTransactionRepository(TXN_PATH, accounts, true, 3);
        txns.save(new Transaction(Transaction.generateId(), LocalDateTime.now(), 1001, TransactionType.DEPOSIT,
                10000.0, 60000.0, "SUCCESS", "Deposit, branch counter", 0, 0));
        txns.save(new Transaction(Transaction.generateId(), LocalDateTime.now(), 1002, TransactionType.TRANSFER,
                500.0, 39500.0, "SUCCESS", "Transfer to Account #1001", 1002, 1001));
        FileTransactionRepository txnReloaded = new FileTransactionRepository(TXN_PATH, accounts);
        System.out.println("  Found transactions for #1001: " + txnReloaded.findByAccount(1001).size() + " (Expected: 2)");
        System.out.println("  Found transactions for #1002: " + txnReloaded.findByAccount(1002).size() + " (Expected: 1)");
        check(txnReloaded.findByAccount(1001).size() == 2, "#1001 is owner of one and receiver of the other");
        check(txnReloaded.findByAccount(1002).size() == 1, "#1002 has one record");
        check("Deposit, branch counter".equals(txnReloaded.findAll().get(0).getDescription()), "Description with comma should round-trip");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 4] Atomic Transfer via File Repositories:");
        txns.transfer(1001, 1002, 5000.0);
        FileAccountRepository afterTransfer = new FileAccountRepository(ACC_PATH);
        System.out.println("  Sender balance: Rs. " + afterTransfer.findById(1001).getBalance() + " (Expected: 55000.0)");
        System.out.println("  Receiver balance: Rs. " + afterTransfer.findById(1002).getBalance() + " (Expected: 45000.0)");
        check(afterTransfer.findById(1001).getBalance() == 55000.0, "Sender debited on disk");
        check(afterTransfer.findById(1002).getBalance() == 45000.0, "Receiver credited on disk");
        check(new FileTransactionRepository(TXN_PATH).findAll().size() == 3, "TRANSFER record appended");
        int before = txns.findAll().size();
        check(throwsDataAccess(() -> txns.transfer(1001, 1002, 999999.0)), "Insufficient balance should fail");
        check(throwsDataAccess(() -> txns.transfer(1001, 9999, 10.0)), "Missing account should fail");
        FileAccountRepository unchanged = new FileAccountRepository(ACC_PATH);
        check(unchanged.findById(1001).getBalance() == 55000.0 && unchanged.findById(1002).getBalance() == 45000.0,
                "Failed transfers must not change balances");
        check(txns.findAll().size() == before, "Failed transfers must not add records");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 5] Verify Timestamped Backups Created:");
        List<Path> backups = BackupManager.listBackups(ACC_PATH);
        System.out.println("  Found " + backups.size() + " backup files for accounts.csv (max 3).");
        check(backups.size() == 3, "Only the newest 3 backups should be kept");
        String newest = backups.get(backups.size() - 1).getFileName().toString();
        System.out.println("  Newest backup: " + newest);
        check(newest.matches("accounts\\.csv\\.bak\\.\\d{8}_\\d{6}_\\d{3}_\\d{4}"), "Backup name should carry a timestamp");
        System.out.println("  -> PASSED");

        System.out.println("\n[TEST 6] Atomic Write Leaves No Partial File:");
        AtomicFileWriter.write(DIR + "/atomic.txt", List.of("line 1", "line 2"));
        check(Files.readAllLines(Paths.get(DIR + "/atomic.txt")).size() == 2, "File should contain both lines");
        check(!new File(DIR + "/atomic.txt.tmp").exists(), "Temporary file should be renamed away");
        System.out.println("  Wrote atomic.txt via .tmp + rename; no .tmp left behind.");
        System.out.println("  -> PASSED");

        deleteDir();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  ALL ACTIVITY 26 FILE REPOSITORY TESTS PASSED!");
        System.out.println("=".repeat(60));
    }

    private static void deleteDir() throws Exception {
        Path dir = Paths.get(DIR);
        if (!Files.exists(dir)) return;
        try (Stream<Path> files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean throwsDataAccess(Runnable action) {
        try {
            action.run();
            return false;
        } catch (DataAccessException e) {
            System.out.println("  Rejected: " + e.getMessage());
            return true;
        }
    }
}
