package com.juleswhite.module1;

import com.juleswhite.module1.LLM.Prompt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for creating and configuring agents.
 * Provides methods to reduce boilerplate when creating agents with common configurations.
 */
public class Agents {

    /**
     * Creates an agent with tools discovered from the given class's package and uses function calling language.
     *
     * @param toolProviderClass Class whose package will be scanned for tools
     * @param goals List of goals for the agent
     * @return A configured Agent instance with default LLM
     */
    public static Agent createAgent(Class<?> toolProviderClass, List<Goal> goals) {
        return createAgent(toolProviderClass.getPackageName(), goals, new AgentLanguages.FunctionCallingLanguage(), null);
    }

    /**
     * Creates an agent with tools discovered from the given class's package and uses function calling language.
     *
     * @param toolProviderClass Class whose package will be scanned for tools
     * @param goals List of goals for the agent
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createAgent(Class<?> toolProviderClass, List<Goal> goals, LLM llm) {
        return createAgent(toolProviderClass.getPackageName(), goals, new AgentLanguages.FunctionCallingLanguage(), llm);
    }

    /**
     * Creates an agent with tools discovered from multiple packages and uses function calling language.
     *
     * @param packageNames Array of package names to scan for tools
     * @param goals List of goals for the agent
     * @return A configured Agent instance with default LLM
     */
    public static Agent createAgent(String[] packageNames, List<Goal> goals) {
        return createAgent(packageNames, goals, new AgentLanguages.FunctionCallingLanguage(), null);
    }

    /**
     * Creates an agent with tools discovered from multiple packages and uses function calling language.
     *
     * @param packageNames Array of package names to scan for tools
     * @param goals List of goals for the agent
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createAgent(String[] packageNames, List<Goal> goals, LLM llm) {
        return createAgent(packageNames, goals, new AgentLanguages.FunctionCallingLanguage(), llm);
    }

    /**
     * Creates an agent with tools discovered from the given package and custom agent language.
     *
     * @param packageName Package name to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @return A configured Agent instance with default LLM
     */
    public static Agent createAgent(String packageName, List<Goal> goals, AgentLanguage agentLanguage) {
        return createAgent(new String[]{packageName}, goals, agentLanguage, null);
    }

    /**
     * Creates an agent with tools discovered from the given package and custom agent language.
     *
     * @param packageName Package name to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createAgent(String packageName, List<Goal> goals, AgentLanguage agentLanguage, LLM llm) {
        return createAgent(new String[]{packageName}, goals, agentLanguage, llm);
    }

    /**
     * Creates an agent with tools discovered from multiple packages and custom agent language.
     *
     * @param packageNames Array of package names to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @return A configured Agent instance with default LLM
     */
    public static Agent createAgent(String[] packageNames, List<Goal> goals, AgentLanguage agentLanguage) {
        return createAgent(packageNames, goals, agentLanguage, null);
    }

    /**
     * Creates an agent with tools discovered from multiple packages and custom agent language.
     *
     * @param packageNames Array of package names to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createAgent(String[] packageNames, List<Goal> goals, AgentLanguage agentLanguage, LLM llm) {
        // Create the action registry with automatic tool discovery
        ActionRegistry registry = new ActionRegistry();

        // Discover tools from all specified packages
        Arrays.stream(packageNames).forEach(registry::discoverTools);

        // Create the environment
        Environment environment = new Environment(registry);

        // Create or use the provided LLM
        LLM actualLlm = (llm != null) ? llm : new LLM();

        // Create the LLM response generator
        Function<Prompt, String> generateResponse = prompt -> actualLlm.generateResponse(prompt);

        // Create and return the agent with discovered tools
        return new Agent(goals, registry, agentLanguage, environment, generateResponse);
    }

    /**
     * Creates an agent with tools discovered from the given classes and custom agent language.
     *
     * @param toolProviderClasses Array of classes to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @return A configured Agent instance with default LLM
     */
    public static Agent createAgent(Class<?>[] toolProviderClasses, List<Goal> goals, AgentLanguage agentLanguage) {
        return createAgent(toolProviderClasses, goals, agentLanguage, null);
    }

    /**
     * Creates an agent with tools discovered from the given classes and custom agent language.
     *
     * @param toolProviderClasses Array of classes to scan for tools
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createAgent(Class<?>[] toolProviderClasses, List<Goal> goals, AgentLanguage agentLanguage, LLM llm) {
        // Create the action registry with automatic tool discovery
        ActionRegistry registry = new ActionRegistry();

        // Discover tools from all specified classes
        for (Class<?> clazz : toolProviderClasses) {
            registry.discoverTools(clazz.getPackageName());
        }

        // Create the environment
        Environment environment = new Environment(registry);

        // Create or use the provided LLM
        LLM actualLlm = (llm != null) ? llm : new LLM();

        // Create the LLM response generator
        Function<Prompt, String> generateResponse = prompt -> actualLlm.generateResponse(prompt);

        // Create and return the agent with discovered tools
        return new Agent(goals, registry, agentLanguage, environment, generateResponse);
    }

    /**
     * Creates a basic agent with a single tool provider class and JSON action language.
     *
     * @param toolProviderClass Class to scan for tools
     * @param goals List of goals for the agent
     * @return A configured Agent instance with default LLM
     */
    public static Agent createJsonAgent(Class<?> toolProviderClass, List<Goal> goals) {
        return createAgent(
                new Class<?>[]{toolProviderClass},
                goals,
                new AgentLanguages.JsonActionLanguage(),
                null
        );
    }

    /**
     * Creates a basic agent with a single tool provider class and JSON action language.
     *
     * @param toolProviderClass Class to scan for tools
     * @param goals List of goals for the agent
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createJsonAgent(Class<?> toolProviderClass, List<Goal> goals, LLM llm) {
        return createAgent(
                new Class<?>[]{toolProviderClass},
                goals,
                new AgentLanguages.JsonActionLanguage(),
                llm
        );
    }


    public static Memory runAndPrintResults(Agent agent, String userInput, int maxIterations) throws Exception {
        return runAndPrintResults(agent, userInput, maxIterations, new HashMap<>());
    }
    /**
     * Runs an agent and prints the final memory state.
     *
     * @param agent The agent to run
     * @param userInput The input to the agent
     * @param maxIterations Maximum number of iterations
     * @return The final memory state
     * @throws Exception If an error occurs during execution
     */
    public static Memory runAndPrintResults(Agent agent, String userInput, int maxIterations, Map<String,Object> actionContext) throws Exception {
        Memory finalMemory = agent.run(userInput, null, maxIterations, actionContext);

        // Print the final memory state
        System.out.println("\nFinal Memory State:");
        for (Map<String, Object> item : finalMemory.getMemories()) {
            System.out.println(item.get("type") + ": " + item.get("content"));
        }

        return finalMemory;
    }

    /**
     * Creates an agent with tools discovered from an object instance and its class's package.
     *
     * @param toolInstance Object instance to discover tools from
     * @param goals List of goals for the agent
     * @return A configured Agent instance with default LLM
     */
    public static Agent createInstanceAgent(Object toolInstance, List<Goal> goals) {
        return createInstanceAgent(toolInstance, goals, new AgentLanguages.FunctionCallingLanguage(), null);
    }

    /**
     * Creates an agent with tools discovered from an object instance and its class's package.
     *
     * @param toolInstance Object instance to discover tools from
     * @param goals List of goals for the agent
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createInstanceAgent(Object toolInstance, List<Goal> goals, LLM llm) {
        return createInstanceAgent(toolInstance, goals, new AgentLanguages.FunctionCallingLanguage(), llm);
    }

    /**
     * Creates an agent with tools discovered from an object instance and custom agent language.
     *
     * @param toolInstance Object instance to discover tools from
     * @param goals List of goals for the agent
     * @param agentLanguage AgentLanguage implementation to use
     * @param llm LLM instance to use for generating responses, or null for default
     * @return A configured Agent instance
     */
    public static Agent createInstanceAgent(Object toolInstance, List<Goal> goals,
                                            AgentLanguage agentLanguage, LLM llm) {
        // Create the action registry
        ActionRegistry registry = new ActionRegistry();

        // Discover tools from the class's package (for static methods)
        registry.discoverInstanceTools(toolInstance);

        // Discover instance tools from the provided object
        registry.discoverInstanceTools(toolInstance);

        // Create the environment
        Environment environment = new Environment(registry);

        // Create or use the provided LLM
        LLM actualLlm = (llm != null) ? llm : new LLM();

        // Create the LLM response generator
        Function<Prompt, String> generateResponse = prompt -> actualLlm.generateResponse(prompt);

        // Create and return the agent
        return new Agent(goals, registry, agentLanguage, environment, generateResponse);
    }
}