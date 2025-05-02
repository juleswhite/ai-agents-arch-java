package com.juleswhite.module1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Memory {
    private final List<Map<String, Object>> items;

    public Memory() {
        this.items = new ArrayList<>();
    }

    public void addMemory(Map<String, Object> memory) {
        items.add(memory);
    }

    public List<Map<String, Object>> getMemories() {
        return getMemories(null);
    }

    public List<Map<String, Object>> getMemories(Integer limit) {
        if (limit == null || limit >= items.size()) {
            return new ArrayList<>(items);
        }
        return new ArrayList<>(items.subList(0, limit));
    }
}