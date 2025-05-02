package com.juleswhite.module3;

import com.juleswhite.module1.*;

import java.util.HashMap;
import java.util.Map;

/**
 * A function that takes three parameters and returns a result
 */
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}

public class AgentRegistry {
    private final Map<String, TriFunction<String, Memory, Map<String, Object>, Memory>> agents;

    public AgentRegistry() {
        this.agents = new HashMap<>();
    }

    public void registerAgent(String name, TriFunction<String, Memory, Map<String, Object>, Memory> runFunction) {
        /**
         * Register an agent's run function.
         */
        this.agents.put(name, runFunction);
    }

    public TriFunction<String, Memory, Map<String, Object>, Memory> getAgent(String name) {
        /**
         * Get an agent's run function by name.
         */
        return this.agents.get(name);
    }
}
