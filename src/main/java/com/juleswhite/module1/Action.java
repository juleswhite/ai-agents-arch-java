package com.juleswhite.module1;

import java.util.HashMap;
import java.util.Map;

public class Action {
    private final Tool tool;
    private Map<String, Object> args;

    public Action(Tool tool) {
        this.tool = tool;
        this.args = new HashMap<>();
    }

    public String getToolName() {
        return tool.getToolName();
    }

    public String getDescription() {
        return tool.getDescription();
    }

    public Map<String, Object> getParameters() {
        return tool.getParameters();
    }

    public boolean isTerminal() {
        return tool.isTerminal();
    }

    public Tool getTool() {
        return tool;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}