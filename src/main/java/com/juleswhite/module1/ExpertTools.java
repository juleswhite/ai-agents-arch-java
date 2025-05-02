package com.juleswhite.module1;

import com.juleswhite.module1.LLM.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpertTools {

    protected final LLM llm;

    public ExpertTools(LLM llm) {
        this.llm = llm;
    }

    public ExpertTools() {
        this(new LLM());
    }

    @RegisterTool
    public String promptExpert(String descriptionOfExpert, String prompt) {
        /**
         * Generate a response from an expert persona.
         *
         * The expert's background and specialization should be thoroughly described to ensure
         * responses align with their expertise. The prompt should be focused on topics within
         * their domain of knowledge.
         *
         * @param descriptionOfExpert Detailed description of the expert's background and expertise
         * @param prompt The specific question or task for the expert
         * @return The expert's response
         */
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system",
                "Act as the following expert and respond accordingly: " + descriptionOfExpert));
        messages.add(new Message("user", prompt));

        Prompt expertPrompt = new Prompt(messages, null);

        return this.llm.generateResponse(expertPrompt);
    }
}