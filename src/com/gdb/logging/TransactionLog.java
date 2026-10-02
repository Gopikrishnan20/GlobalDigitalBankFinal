package com.gdb.logging;

import com.gdb.command.TransactionCommand;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TransactionLog {
    private static final Path LOG_FILE = Path.of("data", "transactions.ser");

    public void log(TransactionCommand command) throws IOException {
        Files.createDirectories(LOG_FILE.getParent());
        boolean append = Files.exists(LOG_FILE);
        try (ObjectOutputStream output = append
                ? new AppendableObjectOutputStream(Files.newOutputStream(LOG_FILE, StandardOpenOption.APPEND))
                : new ObjectOutputStream(Files.newOutputStream(LOG_FILE, StandardOpenOption.CREATE_NEW))) {
            output.writeObject(command);
            output.flush();
        }
    }

    public List<TransactionCommand> readAll() throws IOException, ClassNotFoundException {
        List<TransactionCommand> commands = new ArrayList<>();
        if (!Files.exists(LOG_FILE) || Files.size(LOG_FILE) == 0) {
            return commands;
        }

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(LOG_FILE))) {
            while (true) {
                try {
                    commands.add((TransactionCommand) input.readObject());
                } catch (EOFException endOfFile) {
                    break;
                }
            }
        }
        return commands;
    }

    public void clear() throws IOException {
        Files.deleteIfExists(LOG_FILE);
    }

    private static class AppendableObjectOutputStream extends ObjectOutputStream {
        private AppendableObjectOutputStream(OutputStream output) throws IOException {
            super(output);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }
}