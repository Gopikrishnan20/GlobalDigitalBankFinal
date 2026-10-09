package com.gdb.service;

import com.gdb.domain.*;
import com.gdb.exceptions.*;
import com.gdb.command.*;
import com.gdb.logging.TransactionLogger;
import com.gdb.repository.AccountRepository;
import com.gdb.repository.TransactionRepository;
import com.gdb.repository.RepositoryFactory;

import java.util.*;

/**
 * Service orchestrating banking operations, business rules validation, and repository persistence.
 */
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLogger logger;

    public AccountService(AccountRepository accountRepository, 
                          TransactionRepository transactionRepository, 
                          TransactionLogger logger) {
        this.accountRepository = Objects.requireNonNull(accountRepository, "accountRepository must not be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository must not be null");
        this.logger = logger;
    }

    public AccountService(TransactionLogger logger) {
        this(RepositoryFactory.getAccountRepository(), 
             RepositoryFactory.getTransactionRepository(), 
             logger);
    }

    public IAccount openAccount(String type, String name, int age, double initialBalance, int tenureYears) 
            throws AccountException {
        int accountNumber = accountRepository.nextAccountNumber();
        IAccount account = AccountFactory.createAccount(type, accountNumber, name, age, initialBalance, tenureYears);
        accountRepository.save(account);
        return account;
    }

    public IAccount openAccount(String type, String name, int age, double initialBalance) 
            throws AccountException {
        return openAccount(type, name, age, initialBalance, 0);
    }

    public IAccount getAccount(int accountNumber) {
        return accountRepository.findById(accountNumber);
    }

    public Transaction deposit(int accountNumber, double amount) throws Exception {
        IAccount account = getAccount(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        DepositCommand cmd = new DepositCommand(account, amount);
        cmd.execute();
        accountRepository.update(account);
        if (cmd.getTransaction() != null) {
            transactionRepository.save(cmd.getTransaction());
        }
        if (logger != null) {
            logger.log(cmd);
        }
        return cmd.getTransaction();
    }

    public Transaction withdraw(int accountNumber, double amount, int pin) throws Exception {
        IAccount account = getAccount(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        WithdrawCommand cmd = new WithdrawCommand(account, amount, pin);
        cmd.execute();
        accountRepository.update(account);
        if (cmd.getTransaction() != null) {
            transactionRepository.save(cmd.getTransaction());
        }
        if (logger != null) {
            logger.log(cmd);
        }
        return cmd.getTransaction();
    }

    public Transaction transfer(int fromAccountNumber, int toAccountNumber,
                                double amount, int pin) throws Exception {
        IAccount fromAccount = getAccount(fromAccountNumber);
        IAccount toAccount = getAccount(toAccountNumber);
        if (fromAccount == null) {
            throw new AccountException("Source account not found: " + fromAccountNumber);
        }
        if (toAccount == null) {
            throw new AccountException("Destination account not found: " + toAccountNumber);
        }

        TransferCommand cmd = new TransferCommand(fromAccount, toAccount, amount, pin);
        cmd.execute();
        accountRepository.update(fromAccount);
        accountRepository.update(toAccount);
        if (cmd.getTransaction() != null) {
            transactionRepository.save(cmd.getTransaction());
        }
        if (logger != null) {
            logger.log(cmd);
        }
        return cmd.getTransaction();
    }

    public void closeAccount(int accountNumber, int pin) throws AccountException {
        IAccount account = getAccount(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        if (!account.verifyPin(pin)) {
            throw new InvalidPinException("Incorrect PIN");
        }
        account.closeAccount();
        accountRepository.update(account);
    }

    public List<TransactionCommand> getTransactionHistory() {
        return logger != null ? logger.readAll() : new ArrayList<>();
    }

    public List<Transaction> getTransactionRecords() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionRecords(int accountNumber) {
        return transactionRepository.findByAccount(accountNumber);
    }

    public List<IAccount> getAllAccounts() {
        return accountRepository.findAll();
    }
}
