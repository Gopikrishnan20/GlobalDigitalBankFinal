package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class FileLogDestination implements LogDestination {
    private TransactionLog log;

    public FileLogDestination() {
        this.log = new TransactionLog();
    }

    @Override
    public void write(TransactionCommand cmd) {
        try {
            log.log(cmd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<TransactionCommand> readAll() {
        try {
            return log.readAll();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void clear() {
        try {
            log.clear();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getDestinationName() {
        return "FILE";
    }
}
