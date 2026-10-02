package com.gdb.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulatedDatabase {
    private final Map<String, List<Object>> tables = new HashMap<>();

    public void insert(String table, Object row) {
        tables.computeIfAbsent(table, name -> new ArrayList<>()).add(row);
    }

    public List<Object> selectAll(String table) {
        return new ArrayList<>(tables.getOrDefault(table, List.of()));
    }

    public void deleteAll(String table) {
        tables.remove(table);
    }
}
