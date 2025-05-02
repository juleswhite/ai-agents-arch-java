package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.FunctionDefinition;
import com.openai.models.chat.completions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LLM {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String model;

    /**
     * Class to represent a prompt for the LLM, including messages and optional tools
     */
    public static class Prompt {
        private List<Message> messages;
        private List<Tool> tools;
        private Map<String, Object> metadata;


        public Prompt(List<Message> messages) {
            this.messages = messages;
            this.tools = new ArrayList<>();
            this.metadata = new HashMap<>();
        }

        public Prompt(List<Message> messages, List<Tool> tools) {
            this.messages = messages;
            this.tools = tools != null ? tools : new ArrayList<>();
            this.metadata = new HashMap<>();
        }

        public Prompt(List<Message> messages, List<Tool> tools, Map<String, Object> metadata) {
            this.messages = messages;
            this.tools = tools != null ? tools : new ArrayList<>();
            this.metadata = metadata != null ? metadata : new HashMap<>();
        }

        public List<Message> getMessages() {
            return messages;
        }

        public List<Tool> getTools() {
            return tools;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }
    }

    public LLM() {
        this.model = ChatModel.GPT_4_1.asString();
    }

    public LLM(String model) {
        this.model = model;
    }

    /**
     * Generates an LLM response based on the provided prompt.
     *
     * @param prompt A Prompt object containing messages, optional tools, and metadata.
     * @return The generated response as a String.
     */
    public String generateResponse(Prompt prompt) {
        try {
            // Initialize OpenAI client using environment variables
            OpenAIClient client = OpenAIOkHttpClient.fromEnv();

            List<Message> messages = prompt.getMessages();
            List<Tool> tools = prompt.getTools();

            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                    .model(this.model)
                    .maxTokens(1024);

            // Add messages to the request
            for (Message message : messages) {
                if (message.getRole().equals("system")) {
                    ChatCompletionSystemMessageParam systemMsg = ChatCompletionSystemMessageParam.builder()
                            .content(message.getContent())
                            .build();
                    paramsBuilder.addMessage(systemMsg);
                } else if (message.getRole().equals("user")) {
                    ChatCompletionUserMessageParam userMsg = ChatCompletionUserMessageParam.builder()
                            .content(message.getContent())
                            .build();
                    paramsBuilder.addMessage(userMsg);
                } else {
                    ChatCompletionAssistantMessageParam assistantMsg = ChatCompletionAssistantMessageParam.builder()
                            .content(message.getContent())
                            .build();
                    paramsBuilder.addMessage(assistantMsg);
                }
            }

            String result = null;

            // Handle cases with and without tools
            if (tools.isEmpty()) {
                // No tools, just get normal completion
                ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());
                result = completion.choices().get(0).message().content().orElse("");
            } else {
                // Add tools to the request
                List<ChatCompletionTool> chatCompletionTools = convertToolsToOpenAIFormat(tools);
                paramsBuilder.tools(chatCompletionTools);

                // Get completion with tools
                ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());

                // Check if the model used a tool
                if (completion.choices().get(0).message().toolCalls() != null &&
                        !completion.choices().get(0).message().toolCalls().isEmpty()) {

                    // Extract the tool call
                    ChatCompletionMessageToolCall toolCall = completion.choices().get(0).message().toolCalls().get().get(0);

                    // Format the response as a JSON string
                    Map<String, Object> toolResponse = new HashMap<>();
                    toolResponse.put("tool", toolCall.function().name());
                    toolResponse.put("args", objectMapper.readValue(toolCall.function().arguments(), Map.class));

                    result = objectMapper.writeValueAsString(toolResponse);
                } else {
                    // Model chose to respond with text instead of using a tool
                    result = completion.choices().get(0).message().content().orElse("");
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            e.printStackTrace();

            System.out.println("Prompt details:");
            for (Message message : prompt.getMessages()) {
                System.out.println("Message: " + message.getRole() + " - " + message.getContent());
            }

            if (!prompt.getTools().isEmpty()) {
                System.out.println("Tools:");
                for (Tool tool : prompt.getTools()) {
                    System.out.println("Tool: " + tool.getToolName() + " - " + tool.getDescription());
                }
            }

            System.out.println("Model: " + this.model);

            throw new RuntimeException("Failed to generate response", e);
        }
    }

    /**
     * Convenience method to generate a response from just messages
     */
    public String generateResponse(List<Message> messages) {
        return generateResponse(new Prompt(messages));
    }

    /**
     * Converts our Tool objects to OpenAI's ChatCompletionTool format
     */
    private List<ChatCompletionTool> convertToolsToOpenAIFormat(List<Tool> tools) {
        List<ChatCompletionTool> chatCompletionTools = new ArrayList<>();

        for (Tool tool : tools) {
            ChatCompletionTool chatCompletionTool = ChatCompletionTool.builder()
                    .type(JsonValue.from("function"))
                    .function(FunctionDefinition.builder()
                            .name(tool.getToolName())
                            .description(tool.getDescription())
                            .parameters(JsonValue.from(tool.getParameters()))
                            .build())
                    .build();

            chatCompletionTools.add(chatCompletionTool);
        }

        return chatCompletionTools;
    }
}