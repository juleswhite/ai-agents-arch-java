package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class ToolDiscovery {

    public static class RegisteredTool {

        public final Class <?> originClass;
        public final Method originMethod;
        public final Tool tool;

        public RegisteredTool(Class<?> originClass, Method originMethod, Tool tool) {
            this.originClass = originClass;
            this.originMethod = originMethod;
            this.tool = tool;
        }

    }

    private static final Map<String, RegisteredTool> tools = new HashMap<>();
    private static final Map<String, List<String>> toolsByTag = new HashMap<>();

    /**
     * Scans the specified packages for @RegisterTool annotations and
     * registers those methods as tools.
     *
     * @param packageNames The packages to scan
     * @return A map of discovered tools
     */
    public static Map<String, RegisteredTool> discoverTools(String... packageNames) {
        Reflections reflections =  new Reflections(new ConfigurationBuilder()
                .forPackages(packageNames)
                .addScanners(Scanners.MethodsAnnotated));

        // Find all methods annotated with @RegisterTool
        Set<Method> methods = reflections.getMethodsAnnotatedWith(RegisterTool.class);

        for (Method method : methods) {
            RegisterTool annotation = method.getAnnotation(RegisterTool.class);
            Tool tool = createToolFromMethod(method, annotation);

            // Register the tool
            tools.put(tool.getToolName(), new RegisteredTool(method.getDeclaringClass(), method, tool));

            // Register by tags
            for (String tag : annotation.tags()) {
                toolsByTag.computeIfAbsent(tag, k -> new ArrayList<>())
                        .add(tool.getToolName());
            }
        }

        return tools;
    }

    /**
     * Discovers instance methods with @RegisterTool annotations in a class.
     *
     * @param clazz The class to scan for annotated instance methods
     * @return Map of tool names to RegisteredTool objects
     */
    public static Map<String, RegisteredTool> discoverInstanceTools(Class<?> clazz) {
        Map<String, RegisteredTool> result = new HashMap<>();

        // Get all declared methods, including instance methods
        for (Method method : clazz.getDeclaredMethods()) {
            RegisterTool annotation = method.getAnnotation(RegisterTool.class);
            if (annotation == null) {
                continue;
            }

            // Skip static methods - we only want instance methods
            if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            Tool tool = createToolFromMethod(method, annotation);
            result.put(tool.getToolName(), new RegisteredTool(method.getDeclaringClass(), method, tool));
        }

        return result;
    }

    /**
     * Creates a Tool object from an annotated method.
     */
    private static Tool createToolFromMethod(Method method, RegisterTool annotation) {
        // Get tool name (method name by default)
        String toolName = annotation.name().isEmpty() ?
                method.getName() : annotation.name();

        // Get description (extract from Javadoc if not specified)
        String description = annotation.description().isEmpty() ?
                extractJavadoc(method) : annotation.description();

        // Build parameters schema from method signature
        Map<String, Object> parameters = buildParametersSchema(method, annotation);

        // Create and return the tool
        return new Tool(
                toolName,
                description,
                parameters,
                annotation.terminal()
        );
    }

    /**
     * Extracts Javadoc from a method (simplified implementation).
     * In a real implementation, this would parse the Javadoc from source or use
     * a Javadoc doclet during compilation.
     */
    private static String extractJavadoc(Method method) {
        // This is a placeholder - real implementation would be more complex
        return "Description for " + method.getName();
    }

    /**
     * Builds a JSON Schema for the method parameters.
     */
    private static Map<String, Object> buildParametersSchema(Method method, RegisterTool annotation) {

        if (!annotation.parameters().isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(annotation.parameters(), Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse parameters schema", e);
            }
        }

        // Fall back to auto-discovery of parameters schema
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            // Skip special parameters
            if (param.getName().equals("actionContext") ||
                    param.getName().equals("actionAgent") ||
                    param.getName().startsWith("_")) {
                continue;
            }

            // Add property for this parameter
            Map<String, Object> paramSchema = new HashMap<>();
            paramSchema.put("type", getJsonType(param.getType()));
            properties.put(param.getName(), paramSchema);

            // If parameter doesn't have default value (not possible to check in Java reflection)
            // assume it's required
            required.add(param.getName());
        }

        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    /**
     * Converts a Java type to a JSON Schema type.
     */
    private static String getJsonType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Short.class || type == short.class ||
                type == Byte.class || type == byte.class) {
            return "integer";
        } else if (type == Float.class || type == float.class ||
                type == Double.class || type == double.class) {
            return "number";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "object";
        }
    }

    /**
     * Gets all registered tools.
     */
    public static Map<String, RegisteredTool> getAllTools() {
        return tools;
    }

    /**
     * Gets tools by tag.
     */
    public static List<RegisteredTool> getToolsByTag(String tag) {
        return toolsByTag.getOrDefault(tag, Collections.emptyList())
                .stream()
                .map(tools::get)
                .collect(Collectors.toList());
    }
}