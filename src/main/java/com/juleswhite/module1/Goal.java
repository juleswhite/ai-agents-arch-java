package com.juleswhite.module1;

public class Goal {
    private final int priority;
    private final String name;
    private final String description;

    public Goal(int priority, String name, String description) {
        this.priority = priority;
        this.name = name;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}