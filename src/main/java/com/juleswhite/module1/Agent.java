package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.function.Function;
import com.juleswhite.module1.LLM.Prompt;

public class Agent {

    private final List<Goal> goals;
    private final ActionRegistry actions;
    private final AgentLanguage agentLanguage;
    private final Environment environment;
    private final Function<Prompt, String> generateResponse;

    public Agent(List<Goal> goals,
                 ActionRegistry actions,
                 AgentLanguage agentLanguage,
                 Environment environment,
                 Function<Prompt, String> generateResponse) {
        this.goals = goals;
        this.actions = actions;
        this.agentLanguage = agentLanguage;
        this.environment = environment;
        this.generateResponse = generateResponse;
    }

    public Prompt constructPrompt(List<Goal> goals, Memory memory, ActionRegistry actions) {
        return agentLanguage.constructPrompt(
                actions.getTools(),
                environment,
                goals,
                memory
        );
    }

    public ActionRegistry getActionRegistry() {
        return actions;
    }

    public Action parseAction(String response) throws Exception {
        Map<String, Object> invocation = agentLanguage.parseResponse(response);
        String toolName = (String) invocation.get("tool");
        Map<String, Object> args = (Map<String, Object>) invocation.get("args");

        Action action = actions.getAction(toolName, args);
        return action;
    }

    public boolean shouldTerminate(String response) throws Exception {
        Action action = parseAction(response);
        return action.isTerminal();
    }

    public void setCurrentTask(Memory memory, String task) {
        Map<String, Object> taskMemory = new HashMap<>();
        taskMemory.put("type", "user");
        taskMemory.put("content", task);
        memory.addMemory(taskMemory);
    }

    public void updateMemory(Memory memory, String response, Map<String, Object> result) throws Exception {
        Map<String, Object> responseMemory = new HashMap<>();
        responseMemory.put("type", "assistant");
        responseMemory.put("content", response);
        memory.addMemory(responseMemory);

        Map<String, Object> resultMemory = new HashMap<>();
        resultMemory.put("type", "user"); // The "user" is the other party in the conversation, which is the "computer"
        resultMemory.put("content", new ObjectMapper().writeValueAsString(result));
        memory.addMemory(resultMemory);
    }

    public String promptLLMForAction(Prompt fullPrompt) {
        return generateResponse.apply(fullPrompt);
    }

    public Memory run(String userInput, Memory memory, int maxIterations) throws Exception {
        return run(userInput, memory, maxIterations, new HashMap<>());
    }

    public Memory run(String userInput, Memory memory, int maxIterations, Map<String,Object> actionContext) throws Exception {
        memory = memory != null ? memory : new Memory();
        setCurrentTask(memory, userInput);

        for (int i = 0; i < maxIterations; i++) {
            // Construct a prompt that includes the Goals, Actions, and the current Memory
            Prompt prompt = constructPrompt(goals, memory, actions);

            System.out.println("Agent thinking...");
            // Generate a response from the agent
            String response = promptLLMForAction(prompt);
            System.out.println("Agent Decision: " + response);

            // Determine which action the agent wants to execute
            Action action = parseAction(response);

            // Execute the action in the environment
            Map<String, Object> result = environment.executeAction(action, actionContext);
            System.out.println("Action Result: " + result);

            // Update the agent's memory with information about what happened
            updateMemory(memory, response, result);

            // Check if the agent has decided to terminate
            if (shouldTerminate(response)) {
                break;
            }
        }

        return memory;
    }
}