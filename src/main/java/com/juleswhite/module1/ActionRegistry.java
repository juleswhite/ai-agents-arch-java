package com.juleswhite.module1;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ActionRegistry {
    private final Map<String, Tool> tools;
    private final Map<String, Object> toolBindings;

    public ActionRegistry() {
        this.tools = new HashMap<>();
        this.toolBindings = new HashMap<>();
    }

    public void register(Tool tool, Object binding) {
        tools.put(tool.getToolName(), tool);
        toolBindings.put(tool.getToolName(), binding);
    }

    public Action getAction(String toolName) {
        Tool tool = tools.get(toolName);
        if (tool != null) {
            return new Action(tool);
        }
        return null;
    }

    public Action getAction(String toolName, Map<String, Object> args) {
        Action action = getAction(toolName);
        if (action != null) {
            action.setArgs(args);
        }
        return action;
    }

    public Object getBinding(String toolName) {
        return toolBindings.get(toolName);
    }

    public List<Tool> getTools() {
        return new ArrayList<>(tools.values());
    }

    public void discoverTools(String... packageNames) {
        Map<String, ToolDiscovery.RegisteredTool> discoveredTools = ToolDiscovery.discoverTools(packageNames);

        for (Map.Entry<String, ToolDiscovery.RegisteredTool> entry : discoveredTools.entrySet()) {
            String toolName = entry.getKey();
            ToolDiscovery.RegisteredTool tool = entry.getValue();

            // Find the method that this tool was created from
            try {

                // Create a function that invokes this method
                Function<Map<String, Object>, Object> binding = args -> {
                    try {
                        // Convert arguments to method parameters
                        Object[] methodArgs = Environment.prepareMethodArguments(tool.originMethod, args);

                        // Invoke the method
                        return tool.originMethod.invoke(null, methodArgs);
                    } catch (Exception e) {
                        return "Error invoking " + toolName + ": " + e.getMessage();
                    }
                };

                // Register the tool with its binding
                register(tool.tool, binding);

            } catch (Exception e) {
                System.err.println("Failed to create binding for tool " + toolName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Finds a method by name in a class.
     */
    private Method findMethodByName(Class<?> clazz, String methodName) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Method " + methodName + " not found in " + clazz.getName());
    }

    /**
            * Discovers and registers tools from an object instance's methods.
            * This allows binding instance methods with the @RegisterTool annotation.
            *
            * @param instance The object instance to discover tools from
     */
    public void discoverInstanceTools(Object instance) {
        Class<?> clazz = instance.getClass();
        Map<String, ToolDiscovery.RegisteredTool> discoveredTools =
                ToolDiscovery.discoverInstanceTools(clazz);

        for (Map.Entry<String, ToolDiscovery.RegisteredTool> entry : discoveredTools.entrySet()) {
            String toolName = entry.getKey();
            ToolDiscovery.RegisteredTool tool = entry.getValue();

            // Create a function that invokes this method on the provided instance
            Function<Map<String, Object>, Object> binding = args -> {
                try {
                    // Convert arguments to method parameters
                    Object[] methodArgs = Environment.prepareMethodArguments(tool.originMethod, args);

                    // Invoke the method on the instance
                    return tool.originMethod.invoke(instance, methodArgs);
                } catch (Exception e) {
                    return "Error invoking " + toolName + ": " + e.getMessage();
                }
            };

            // Register the tool with its binding
            register(tool.tool, binding);
        }
    }

}