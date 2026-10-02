package com.gdb.command;

import com.gdb.domain.IAccount;
import com.gdb.domain.Transaction;
import com.gdb.service.TransferService;

public class TransferCommand implements TransactionCommand {
    private static final long serialVersionUID = 1L;

    private final IAccount from;
    private final IAccount to;
    private final double amount;
    private final int pin;
    private Transaction transaction;

    public TransferCommand(IAccount from, IAccount to, double amount, int pin) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.pin = pin;
    }

    @Override
    public void execute() throws Exception {
        transaction = new TransferService().transferWithTransaction(from, to, amount, pin);
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}