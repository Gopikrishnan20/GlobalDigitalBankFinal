package com.gdb;

import com.gdb.logging.FileLogDestination;
import com.gdb.logging.TransactionLogger;
import com.gdb.service.AccountService;
import com.gdb.ui.AccountUI;

public class Main {
    public static void main(String[] args) {
        TransactionLogger logger = new TransactionLogger(new FileLogDestination());
        logger.clear();
        AccountService service = new AccountService(logger);
        new AccountUI(service).start();
    }
}
