package com.gdb.service;

import com.gdb.command.DepositCommand;
import com.gdb.command.TransactionCommand;
import com.gdb.command.TransferCommand;
import com.gdb.command.WithdrawCommand;
import com.gdb.domain.Account;
import com.gdb.domain.AccountFactory;
import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.exceptions.AccountException;
import com.gdb.exceptions.InvalidPinException;
import com.gdb.logging.TransactionLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountService {
    private final Map<Integer, IAccount> accounts;
    private final TransactionLogger logger;
    private final TransferService transferService;
    private int nextAccountNumber = 1001;

    public AccountService(TransactionLogger logger) {
        this.accounts = new LinkedHashMap<>();
        this.logger = logger;
        this.transferService = new TransferService();
    }

    public IAccount openAccount(String type, String name, int age, double initialBalance)
            throws AccountException {
        int accountNumber = nextAccountNumber;
        IAccount account = AccountFactory.createAccount(type, accountNumber, name, age, initialBalance);
        accounts.put(accountNumber, account);
        nextAccountNumber++;
        return account;
    }

    public void closeAccount(int accountNumber, int pin) throws AccountException {
        IAccount account = findAccount(accountNumber);
        if (!account.verifyPin(pin)) {
            throw new InvalidPinException("Incorrect PIN");
        }
        account.closeAccount();
    }

    public Transaction deposit(int accountNumber, double amount) throws Exception {
        Account account = (Account) findAccount(accountNumber);
        return executeAndLog(new DepositCommand(account, amount));
    }

    public Transaction withdraw(int accountNumber, double amount, int pin) throws Exception {
        Account account = (Account) findAccount(accountNumber);
        return executeAndLog(new WithdrawCommand(account, amount, pin));
    }

    public Transaction transfer(int fromAcc, int toAcc, double amount, int pin) throws Exception {
        IAccount from = findAccount(fromAcc);
        IAccount to = findAccount(toAcc);
        return executeAndLog(new TransferCommand(from, to, amount, pin));
    }

    public IAccount getAccount(int accNo) {
        return accounts.get(accNo);
    }

    public List<IAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public List<Transaction> getTransactionHistory() {
        List<Transaction> history = new ArrayList<>();
        for (TransactionCommand command : logger.readAll()) {
            history.add(command.getTransaction());
        }
        return history;
    }

    public int getNextAccountNumber() {
        return nextAccountNumber;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    private IAccount findAccount(int accountNumber) throws AccountException {
        IAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountException("Account not found: " + accountNumber);
        }
        return account;
    }

    private Transaction executeAndLog(TransactionCommand command) throws Exception {
        command.execute();
        logger.log(command);
        return command.getTransaction();
    }
}
