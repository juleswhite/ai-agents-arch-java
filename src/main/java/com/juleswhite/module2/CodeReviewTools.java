package com.juleswhite.module2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juleswhite.module1.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CodeReviewTools {

    @RegisterTool(
            description = "Analyze code quality and suggest improvements",
            tags = {"code_quality"}
    )
    public String analyzeCodeQuality(Map<String,Object> actionContext, String code) {
        /**
         * Review code quality and suggest improvements.
         */
        // Get memory to understand the code's context
        Memory memory = (Memory) actionContext.get("memory");

        // Extract relevant history
        List<String> developmentContext = new ArrayList<>();
        for (Map<String, Object> mem : memory.getMemories()) {
            String type = (String) mem.get("type");
            String content = (String) mem.get("content");

            if ("user".equals(type)) {
                developmentContext.add("User: " + content);
            }
            // Hypothetical scenario where our agent includes the phrase "Here's the implementation" when it generates code
            else if ("assistant".equals(type) && content.contains("Here's the implementation")) {
                developmentContext.add("Implementation Decision: " + content);
            }
        }

        // Create review prompt with full context
        String reviewPrompt = String.format(
                """
                Review this code in the context of its development history:
                
                Development History:
                %s
                
                Current Implementation:
                %s
                
                Analyze:
                1. Does the implementation meet all stated requirements?
                2. Are all constraints and considerations from the discussion addressed?
                3. Have any requirements or constraints been overlooked?
                4. What improvements could make the code better while staying within the discussed parameters?
                """,
                String.join("\n", developmentContext),
                code
        );
        LLM.Prompt prompt = new LLM.Prompt(List.of(
                new Message("user", reviewPrompt)
        ));

        LLM llm = (LLM) actionContext.get("llm");

        return llm.generateResponse(prompt);
    }

    public static void main(String[] args) throws JsonProcessingException {
        CodeReviewTools codeReviewTools = new CodeReviewTools();
        Map<String, ToolDiscovery.RegisteredTool> tools = ToolDiscovery.discoverInstanceTools(CodeReviewTools.class);

        // Let's see if the schema exludes the ActionContext
        for (Map.Entry<String, ToolDiscovery.RegisteredTool> entry : tools.entrySet()) {
            String name = entry.getKey();
            ToolDiscovery.RegisteredTool tool = entry.getValue();
            System.out.println("Tool Name: " + name);
            System.out.println("Declaring Class: " + tool.originClass);
            System.out.println("Description: " + tool.tool.getDescription());
            System.out.println("Parameters: " + (new ObjectMapper()).writeValueAsString(tool.tool.getParameters()));
            System.out.println();
        }
    }
}
