package com.juleswhite.module1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class Environment {
    private final ActionRegistry registry;

    public Environment(ActionRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> executeAction(Action action) {
        try {
            String toolName = action.getToolName();
            Map<String, Object> args = action.getArgs();
            Object binding = registry.getBinding(toolName);

            Object result;
            if (binding instanceof Function) {
                // If it's a Function interface, just call apply
                @SuppressWarnings("unchecked")
                Function<Map<String, Object>, Object> func = (Function<Map<String, Object>, Object>) binding;
                result = func.apply(args);
            } else {
                // If it's a method or other object, use reflection
                result = invokeMethodWithReflection(binding, args);
            }

            return formatResult(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("tool_executed", false);
            errorResult.put("error", e.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorResult.put("traceback", sw.toString());

            return errorResult;
        }
    }

    public Map<String, Object> formatResult(Object result) {
        Map<String, Object> formattedResult = new HashMap<>();
        formattedResult.put("tool_executed", true);
        formattedResult.put("result", result);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        formattedResult.put("timestamp", sdf.format(new Date()));

        return formattedResult;
    }

    public static Object invokeMethodWithReflection(Object binding, Map<String, Object> args) throws Exception {
        if (binding instanceof Method) {
            // Type 1: Static method binding
            Method method = (Method) binding;
            Object[] methodArgs = prepareArguments(method, args);
            return method.invoke(null, methodArgs);
        } else {
            // Type 2: Object method binding
            // Find appropriate method on the object
            for (Method method : binding.getClass().getMethods()) {
                if (method.getParameterCount() == args.size()) {
                    try {
                        Object[] methodArgs = prepareArguments(method, args);
                        return method.invoke(binding, methodArgs);
                    } catch (Exception e) {
                        // Try next method
                        continue;
                    }
                }
            }
            throw new RuntimeException("No suitable method found for arguments: " + args);
        }
    }

    public static Object[] prepareArguments(Method method, Map<String, Object> args) {
        Parameter[] parameters = method.getParameters();
        Object[] methodArgs = new Object[parameters.length];

        // Try to match parameters by name
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            if (args.containsKey(paramName)) {
                methodArgs[i] = convertToType(args.get(paramName), param.getType());
            }
        }

        // If some parameters are still null, try to match by position
        if (parameters.length == args.size() && args.size() <= 10) {
            // Only attempt positional mapping for small number of args
            List<String> paramNames = new ArrayList<>(args.keySet());
            for (int i = 0; i < parameters.length; i++) {
                if (methodArgs[i] == null && i < paramNames.size()) {
                    methodArgs[i] = convertToType(args.get(paramNames.get(i)), parameters[i].getType());
                }
            }
        }

        return methodArgs;
    }

    public static Object convertToType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // If value is already of the correct type, return it
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Handle primitive types and common conversions
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } else if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.parseDouble(value.toString());
            }
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            } else {
                return Boolean.parseBoolean(value.toString());
            }
        }

        // For collections, arrays, and other complex types...
        // This would need more implementation for a complete solution

        // If no conversion is possible, return the original value
        // and let the method invocation fail if necessary
        return value;
    }

    /**
     * Prepares arguments for method invocation.
     */
    public static Object[] prepareMethodArguments(Method method, Map<String, Object> args) {
        Parameter[] parameters = method.getParameters();
        Object[] methodArgs = new Object[parameters.length];

        if (parameters.length == 1 && parameters[0].getType() == Map.class) {
            // If the method takes a single Map parameter, just pass the args directly
            methodArgs[0] = args;
            return methodArgs;
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Object value = args.get(param.getName());

            // Convert value to parameter type if needed
            methodArgs[i] = convertToType(value, param.getType());
        }

        return methodArgs;
    }

}