package com.juleswhite.module1;

import com.juleswhite.module1.LLM.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicExpertTools extends ExpertTools {

    public DynamicExpertTools(LLM llm) {
        super(llm);
    }

    public DynamicExpertTools() {
        super();
    }

    @RegisterTool(tags = {"expert_creation"})
    public String createAndConsultExpert(String expertiseDomain, String problemDescription) {
        /**
         * Dynamically create and consult an expert persona based on the specific domain and problem.
         *
         * @param expertiseDomain The specific domain of expertise needed
         * @param problemDescription Detailed description of the problem to be solved
         * @return The expert's insights and recommendations
         */

        // Step 1: Dynamically generate a persona description
        String personaDescriptionPrompt = """
            Create a detailed description of an expert in %s who would be 
            ideally suited to address the following problem:
            
            %s
            
            Your description should include:
            - The expert's background and experience
            - Their specific areas of specialization within %s
            - Their approach to problem-solving
            - The unique perspective they bring to this type of challenge
            """.formatted(expertiseDomain, problemDescription, expertiseDomain);

        List<Message> descriptionMessages = new ArrayList<>();
        descriptionMessages.add(new Message("user", personaDescriptionPrompt));
        Prompt descriptionPrompt = new Prompt(descriptionMessages, null);
        String personaDescription = this.llm.generateResponse(descriptionPrompt);

        System.out.println("====================");
        System.out.println("Expertise Domain:");
        System.out.println(expertiseDomain);
        System.out.println("-------------------");
        System.out.println(personaDescription);
        System.out.println("-------------------");


        // Step 2: Generate a specialized consultation prompt
        String consultationPromptGenerator = """
            Create a detailed consultation prompt for an expert in %s 
            addressing the following problem:
            
            %s
            
            The prompt should guide the expert to provide comprehensive insights and
            actionable recommendations specific to this problem.
            """.formatted(expertiseDomain, problemDescription);

        List<Message> consultationMessages = new ArrayList<>();
        consultationMessages.add(new Message("user", consultationPromptGenerator));
        Prompt consultationPromptRequest = new Prompt(consultationMessages, null);
        String consultationPrompt = this.llm.generateResponse(consultationPromptRequest);

        System.out.println("Consultation Prompt:");
        System.out.println(consultationPrompt);
        System.out.println("====================");
        // Step 3: Consult the dynamically created persona
        return promptExpert(personaDescription, consultationPrompt);
    }

    @RegisterTool(tags = {"development", "workflow"})
    public Map<String, String> developFeature(String featureRequest) {
        /**
         * Process a feature request through a chain of expert personas.
         *
         * @param featureRequest The initial feature request description
         * @return A map containing outputs from each stage of the development process
         */

        // Step 1: Product expert defines requirements
        String requirements = promptExpert(
                "You are a senior product manager with 15 years of experience in software product development. You excel at translating user needs into clear, actionable requirements.",
                "Convert this feature request into detailed requirements: " + featureRequest
        );

        // Step 2: Architecture expert designs the solution
        String architecture = promptExpert(
                "You are a senior software architect with extensive experience designing scalable, maintainable systems. You are skilled at creating clean architectural designs that balance technical excellence with practical constraints.",
                "Design an architecture for these requirements: " + requirements
        );

        // Step 3: Developer expert implements the code
        String implementation = promptExpert(
                "You are a senior developer with deep expertise in writing clean, efficient code. You prioritize readability, performance, and maintainability in your implementations.",
                "Implement code for this architecture: " + architecture
        );

        // Step 4: QA expert creates test cases
        String tests = promptExpert(
                "You are a QA engineering expert with a talent for identifying edge cases and ensuring comprehensive test coverage. You understand both manual and automated testing approaches.",
                "Create test cases for this implementation: " + implementation
        );

        // Step 5: Documentation expert creates documentation
        String documentation = promptExpert(
                "You are a technical documentation specialist who excels at creating clear, comprehensive documentation that is accessible to different audiences while remaining technically accurate.",
                "Document this implementation: " + implementation
        );

        Map<String, String> results = new HashMap<>();
        results.put("requirements", requirements);
        results.put("architecture", architecture);
        results.put("implementation", implementation);
        results.put("tests", tests);
        results.put("documentation", documentation);

        return results;
    }

    public static void main(String[] args) {
        DynamicExpertTools expertTools = new DynamicExpertTools();

        String expertDomain = "Vanderbilt University Professor of Computer Science";

        String problemDescription = "How to implement a new AI-based curriculum for undergraduate students " +
                "now that AI is writing so much code?";

        String expertResponse = expertTools.createAndConsultExpert(expertDomain, problemDescription);
        System.out.println("Expert Response:");
        System.out.println(expertResponse);
    }
}
