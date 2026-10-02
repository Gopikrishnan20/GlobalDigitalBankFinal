package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseLogDestination implements LogDestination {
    private static final String TABLE = "transaction_log";

    private final SimulatedDatabase db;

    public DatabaseLogDestination(SimulatedDatabase db) {
        this.db = db;
    }

    @Override
    public void write(TransactionCommand cmd) {
        db.insert(TABLE, cmd);
    }

    @Override
    public List<TransactionCommand> readAll() {
        return db.selectAll(TABLE).stream()
                .map(TransactionCommand.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        db.deleteAll(TABLE);
    }

    @Override
    public String getDestinationName() {
        return "DATABASE";
    }
}
