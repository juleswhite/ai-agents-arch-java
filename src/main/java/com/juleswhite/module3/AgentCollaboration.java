package com.juleswhite.module3;

import com.juleswhite.module1.*;

import java.util.List;
import java.util.Map;

public class AgentCollaboration {

    /**
     * Main entry point that sets up and demonstrates a multi-agent collaboration system.
     * This example shows how two specialized agents (an email agent and a scheduler agent)
     * can work together to handle a task that spans both of their domains of expertise.
     *
     * @param args Command line arguments (not used in this example)
     * @throws Exception If there's any issue during execution
     */
    public static void main(String[] args) throws Exception {
        // Create a Language Learning Model (LLM) instance that will power both agents
        LLM llm = new LLM();

        // Create the primary Email Agent with its specific tools and goals
        // This agent will be responsible for communicating with users via email
        // and delegating scheduling tasks to the scheduler agent
        Agent agent = Agents.createInstanceAgent(
                new EmailTools(), // Provides email capabilities (send, read, etc.)
                List.of(
                        new Goal(1,
                                "Ask Scheduler", // The agent's primary goal
                                "Call the scheduler agent and ask for . Finally, send an email with a summary of what they tell you.")
                ));

        // Discover and register agent collaboration tools that allow agents to communicate
        // This enables the email agent to call methods on the scheduler agent
        agent.getActionRegistry().discoverInstanceTools(new AgentCollaborationTools());

        // Create a specialized Scheduler Agent that handles calendar and availability operations
        // This agent focuses solely on scheduling-related tasks
        Agent agent2 = Agents.createInstanceAgent(
                new SchedulerTools(), // Provides calendar and scheduling capabilities
                List.of(new Goal(1,
                        "Summarize availability",
                        "check the user's availability always")),
                llm);

        // Create an agent registry to manage all available agents
        // This acts as a directory service where agents can look up other agents
        AgentRegistry registry = new AgentRegistry();

        // Register the scheduler agent so it can be found and called by the email agent
        // The lambda expression defines how to run the scheduler agent with given inputs
        registry.registerAgent("scheduler", (prompt, memory, tools) -> {
            try {
                // Execute the scheduler agent with the provided prompt and return its memory
                return Agents.runAndPrintResults(agent2, prompt, 10);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Create the action context with necessary resources for agent collaboration
        // This context is shared across agent calls and contains crucial resources
        Map<String, Object> actionContext = Map.of(
                "agent_registry", registry, // Allows lookup of other agents
                "llm", llm                  // Provides the language model for reasoning
        );

        // Define the high-level task that will be delegated to the agents
        // This task requires coordination between email and scheduling capabilities
        String task = "Get availability and email it to everyone";

        // Run the primary agent with the given task, maximum iterations, and action context
        // Results are printed to the console as the agents work through the task
        Agents.runAndPrintResults(agent, task, 10, actionContext);
    }

}
