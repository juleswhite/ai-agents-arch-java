package com.juleswhite.module3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.juleswhite.module1.*;
import com.juleswhite.module2.*;

public class AgentCollaborationTools {

    @RegisterTool
    public Map<String, Object> callAgent(Map<String,Object> actionContext,
                                         String agentName,
                                         String task) {
        /**
         * Invoke another agent to perform a specific task.
         *
         * @param actionContext Contains registry of available agents
         * @param agentName Name of the agent to call
         * @param task The task to ask the agent to perform
         * @return The result from the invoked agent's final memory
         */
        // Get the agent registry from our context
        AgentRegistry agentRegistry = (AgentRegistry) actionContext.get("agent_registry");
        if (agentRegistry == null) {
            throw new IllegalArgumentException("No agent registry found in context");
        }

        // Get the agent's run function from the registry
        TriFunction<String, Memory, Map<String, Object>, Memory> agentRun =
                agentRegistry.getAgent(agentName);
        if (agentRun == null) {
            throw new IllegalArgumentException("Agent '" + agentName + "' not found in registry");
        }

        // Create a new memory instance for the invoked agent
        Memory invokedMemory = new Memory();

        try {
            // Create a new context with only the properties needed
            Map<String, Object> contextProps = new HashMap<>();
            // Don't pass agent_registry to prevent infinite recursion

            // Run the agent with the provided task
            Memory resultMemory = agentRun.apply(
                    task,
                    invokedMemory,
                    contextProps
            );

            // Get the last memory item as the result
            List<Map<String, Object>> memories = resultMemory.getMemories();
            if (!memories.isEmpty()) {
                Map<String, Object> lastMemory = memories.get(memories.size() - 1);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("agent", agentName);
                result.put("result", lastMemory.getOrDefault("content", "No result content"));
                return result;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Agent failed to run.");
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
}
