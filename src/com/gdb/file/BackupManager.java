package com.gdb.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates timestamped copies of a data file before it is overwritten, keeping only the newest maxBackups copies.
 */
public class BackupManager {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static int sequence = 0;

    /**
     * Copies path to path.bak.&lt;timestamp&gt; and deletes the oldest backups beyond maxBackups.
     * @return the backup file created, or null if the source file does not exist yet
     */
    public static synchronized Path backup(String path, int maxBackups) throws IOException {
        Path source = Paths.get(path).toAbsolutePath();
        if (!Files.exists(source)) {
            return null;
        }
        String stamp = LocalDateTime.now().format(STAMP) + "_" + String.format("%04d", ++sequence % 10000);
        Path backup = Paths.get(source + ".bak." + stamp);
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);

        List<Path> backups = listBackups(path);
        for (int i = 0; i < backups.size() - Math.max(0, maxBackups); i++) {
            Files.deleteIfExists(backups.get(i));
        }
        return backup;
    }

    /** All backups of path, oldest first (the timestamp format sorts chronologically). */
    public static List<Path> listBackups(String path) throws IOException {
        Path source = Paths.get(path).toAbsolutePath();
        Path dir = source.getParent();
        if (dir == null || !Files.isDirectory(dir)) {
            return new ArrayList<>();
        }
        String prefix = source.getFileName() + ".bak.";
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().startsWith(prefix))
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .collect(Collectors.toList());
        }
    }
}
