package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

// Simplified Tool class for representing tool definitions
public class Tool {

    private String toolName;

    private String description;

    private Map<String, Object> parameters;

    private boolean terminal = false;

    public Tool(){}

    public Tool(String toolName, String description, Map<String, Object> parameters) {
        this.toolName = toolName;
        this.description = description;
        this.parameters = parameters;
    }

    public Tool(String toolName, String description, Map<String, Object> parameters, boolean isTerminal) {
        this.toolName = toolName;
        this.description = description;
        this.parameters = parameters;
        this.terminal = isTerminal;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Converts the Tool object to a JSON string
     *
     * @return JSON string representation of the Tool
     */
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Tool to JSON", e);
        }
    }

    /**
     * Creates a Tool from a JSON string
     *
     * @param json The JSON string representing the tool
     * @return A new Tool instance
     * @throws Exception If the JSON cannot be parsed
     */
    public static Tool fromJson(String json) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Tool.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to Tool", e);
        }
    }
}