package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juleswhite.module1.LLM.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentLanguages {

    public static class FunctionCallingLanguage implements AgentLanguage {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Prompt constructPrompt(
                List<Tool> tools,
                Environment environment,
                List<Goal> goals,
                Memory memory) {
            List<Message> messages = new ArrayList<>();

            // Add formatted goals as a system message
            messages.add(new Message("system", formatGoalsContent(goals)));

            // Add memory messages
            messages.addAll(formatMemory(memory));

            // Create and return the Prompt with messages and tools
            return new Prompt(messages, tools);
        }

        private String formatGoalsContent(List<Goal> goals) {
            // Format goals content
            StringBuilder goalsContent = new StringBuilder("# Goals\n");

            for (Goal goal : goals) {
                goalsContent.append("## ").append(goal.getName()).append("\n");
                goalsContent.append(goal.getDescription()).append("\n\n");
            }

            return goalsContent.toString();
        }

        private List<Message> formatMemory(Memory memory) {
            // Convert memory items to Message objects
            List<Message> messages = new ArrayList<>();

            for (Map<String, Object> memoryItem : memory.getMemories()) {
                String type = (String) memoryItem.get("type");

                // Determine the role based on type
                // We default to "user" for any type that is not "assistant" or "system"
                String role = type.equals("assistant") ? "assistant" :
                        (type.equals("system") ? "system" : "user");

                String content = (String) memoryItem.get("content");
                messages.add(new Message(role, content));
            }

            return messages;
        }

        @Override
        public Map<String, Object> parseResponse(String response) throws Exception {
            // Parse the function call response from LLM's tool format
            try {
                // Our LLM.generateResponse() returns tool calls in the format:
                // {"tool":"toolName","args":{...}}
                Map<String, Object> result = objectMapper.readValue(response, Map.class);

                // Verify expected format
                if (result.containsKey("tool") && result.containsKey("args")) {
                    return result;
                } else {
                    // If response doesn't have expected keys, treat as terminate
                    return createTerminateAction(response);
                }
            } catch (Exception e) {
                // If parsing fails, treat the response as a terminate message
                return createTerminateAction(response);
            }
        }

        private Map<String, Object> createTerminateAction(String message) {
            Map<String, Object> result = new HashMap<>();
            result.put("tool", "terminate");

            Map<String, Object> args = new HashMap<>();
            args.put("message", message);

            result.put("args", args);
            return result;
        }
    }


    public static class JsonActionLanguage implements AgentLanguage {
        private static final String ACTION_FORMAT = """
            <Stop and think step by step. Insert your thoughts here.>
            
            ```action
            {
                "tool": "tool_name",
                "args": {...fill in arguments...}
            }
            ```
            """;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public Prompt constructPrompt(
                List<Tool> tools,
                Environment environment,
                List<Goal> goals,
                Memory memory) {
            List<Message> messages = new ArrayList<>();

            // Add formatted goals as a system message
            messages.add(new Message("system", formatGoalsContent(goals)));

            // Add formatted actions as a system message
            messages.add(new Message("system", formatActionsContent(tools)));

            // Add memory messages
            messages.addAll(formatMemory(memory));

            // Create and return the Prompt
            return new Prompt(messages, tools);
        }

        private String formatActionsContent(List<Tool> tools) {
            // Convert tools to a description the LLM can understand
            List<Map<String, Object>> actionDescriptions = new ArrayList<>();

            for (Tool tool : tools) {
                Map<String, Object> actionDescription = new HashMap<>();
                actionDescription.put("name", tool.getToolName());
                actionDescription.put("description", tool.getDescription());
                actionDescription.put("args", tool.getParameters());

                actionDescriptions.add(actionDescription);
            }

            return "Available Tools: " + toJsonString(actionDescriptions) + "\n\n" + ACTION_FORMAT;
        }

        private String formatGoalsContent(List<Goal> goals) {
            // Format goals content
            StringBuilder goalsContent = new StringBuilder("# Goals\n");

            for (Goal goal : goals) {
                goalsContent.append("## ").append(goal.getName()).append("\n");
                goalsContent.append(goal.getDescription()).append("\n\n");
            }

            return goalsContent.toString();
        }

        private List<Message> formatMemory(Memory memory) {
            // Convert memory items to Message objects
            List<Message> messages = new ArrayList<>();

            for (Map<String, Object> memoryItem : memory.getMemories()) {
                String type = (String) memoryItem.get("type");
                // Determine the role based on type
                // We default to "user" for any type that is not "assistant" or "system"
                String role = type.equals("assistant") ? "assistant" :
                        (type.equals("system") ? "system" : "user");

                String content = (String) memoryItem.get("content");
                messages.add(new Message(role, content));
            }

            return messages;
        }

        private String toJsonString(Object obj) {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } catch (Exception e) {
                return "{}";
            }
        }

        @Override
        public Map<String, Object> parseResponse(String response) throws Exception {
            // Extract and parse the action block
            try {
                String startMarker = "```action";
                String endMarker = "```";

                String strippedResponse = response.trim();
                int startIndex = strippedResponse.indexOf(startMarker);
                int endIndex = strippedResponse.lastIndexOf(endMarker);

                String jsonStr = strippedResponse.substring(
                        startIndex + startMarker.length(), endIndex
                ).trim();

                return objectMapper.readValue(jsonStr, Map.class);
            } catch (Exception e) {
                System.out.println("Failed to parse response: " + e.getMessage());
                throw e;
            }
        }
    }

}
