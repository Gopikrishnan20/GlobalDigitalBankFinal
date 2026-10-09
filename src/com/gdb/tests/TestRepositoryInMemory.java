package com.gdb.tests;

import com.gdb.domain.*;
import com.gdb.repository.*;
import com.gdb.service.AccountService;
import com.gdb.logging.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestRepositoryInMemory {
    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(60));
        System.out.println("  ACTIVITY 21 — REPOSITORY PATTERN (IN-MEMORY)");
        System.out.println("=".repeat(60));

        AccountRepository accountRepo = new InMemoryAccountRepository();
        TransactionRepository txnRepo = new InMemoryTransactionRepository();
        TransactionLogger logger = new TransactionLogger(new MemoryLogDestination());
        AccountService service = new AccountService(accountRepo, txnRepo, logger);

        testAccountRepository(accountRepo);
        testTransactionRepository(txnRepo);
        testFactory();
        testServiceIntegration(service, txnRepo);

        System.out.println("All Activity 21 repository checks passed.");
    }

    private static void testAccountRepository(AccountRepository repository) throws Exception {
        IAccount original = AccountFactory.createAccount("SAVINGS", 2001,
                "Repository User", 30, 10000, 0);
        repository.save(original);
        check(repository.exists(2001), "saved account exists");
        check(repository.findById(2001) == original, "findById returns saved account");
        check(repository.findAll().size() == 1, "findAll returns stored accounts");

        List<IAccount> accountSnapshot = repository.findAll();
        accountSnapshot.clear();
        check(repository.findAll().size() == 1, "findAll returns a defensive list copy");

        IAccount replacement = AccountFactory.createAccount("SAVINGS", 2001,
                "Updated User", 31, 12000, 0);
        repository.update(replacement);
        check(repository.findById(2001) == replacement, "update replaces an existing account");

        IAccount missing = AccountFactory.createAccount("SAVINGS", 2999,
                "Missing User", 31, 12000, 0);
        repository.update(missing);
        check(!repository.exists(2999), "update does not insert a missing account");
        check(repository.nextAccountNumber() == 2002, "account numbers advance past saved IDs");

        repository.delete(2001);
        check(!repository.exists(2001), "delete removes an account");
        System.out.println("[PASS] Account repository CRUD and ID generation");
    }

    private static void testTransactionRepository(TransactionRepository repository) {
        Transaction deposit = transaction(2001, 0, 0);
        Transaction transfer = transaction(2001, 2001, 3002);
        Transaction other = transaction(4004, 0, 0);
        repository.save(deposit);
        repository.save(transfer);
        repository.save(other);
        repository.save(null);

        check(repository.findAll().size() == 3, "findAll returns saved transactions");
        check(repository.findByAccount(2001).size() == 2, "filter finds account-number matches");
        check(repository.findByAccount(3002).size() == 1, "filter finds destination-account matches");

        List<Transaction> transactionSnapshot = repository.findAll();
        transactionSnapshot.clear();
        check(repository.findAll().size() == 3, "findAll returns a defensive list copy");

        repository.clear();
        check(repository.findAll().isEmpty(), "clear removes all transactions");
        System.out.println("[PASS] Transaction repository save, filter, snapshot, and clear");
    }

    private static void testFactory() {
        check("memory".equals(RepositoryFactory.getPersistenceMode()),
                "persistence.mode resolves from configuration");
        check(RepositoryFactory.getAccountRepository() == RepositoryFactory.getAccountRepository(),
                "factory reuses the account repository singleton");
        check(RepositoryFactory.getTransactionRepository()
                        == RepositoryFactory.getTransactionRepository(),
                "factory reuses the transaction repository singleton");
        System.out.println("[PASS] RepositoryFactory configuration and singleton instances");
    }

    private static void testServiceIntegration(AccountService service, TransactionRepository repository)
            throws Exception {
        IAccount source = service.openAccount("SAVINGS", "Source User", 30, 10000);
        IAccount destination = service.openAccount("SAVINGS", "Destination User", 30, 10000);
        source.setPin(1234);

        service.deposit(source.getAccountNumber(), 500);
        service.withdraw(source.getAccountNumber(), 200, 1234);
        service.transfer(source.getAccountNumber(), destination.getAccountNumber(), 100, 1234);

        check(service.getAccount(source.getAccountNumber()).getBalance() == 10200,
                "service persists source balance after operations");
        check(service.getAccount(destination.getAccountNumber()).getBalance() == 10100,
                "service persists destination balance after transfer");
        check(service.getAllAccounts().size() == 2, "service reads accounts through repository");
        check(repository.findAll().size() == 3, "service persists transaction records");
        check(service.getTransactionRecords(source.getAccountNumber()).size() == 3,
                "service reads account transaction history");
        System.out.println("[PASS] AccountService integration with repository interfaces");
    }

    private static Transaction transaction(int accountNumber, int fromAccount, int toAccount) {
        return new Transaction(Transaction.generateId(), LocalDateTime.now(), accountNumber,
                TransactionType.TRANSFER, 100, 900, "SUCCESS", "Test transaction",
                fromAccount, toAccount);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Failed: " + message);
        }
    }
}
