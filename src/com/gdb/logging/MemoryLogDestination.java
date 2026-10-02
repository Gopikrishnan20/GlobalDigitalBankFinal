package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.ArrayList;
import java.util.List;

public class MemoryLogDestination implements LogDestination {
    private final List<TransactionCommand> entries = new ArrayList<>();

    @Override
    public void write(TransactionCommand cmd) {
        entries.add(cmd);
    }

    @Override
    public List<TransactionCommand> readAll() {
        return new ArrayList<>(entries);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public String getDestinationName() {
        return "MEMORY";
    }
}
