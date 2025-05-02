package com.juleswhite.module1;

import java.util.ArrayList;
import java.util.List;

public class DevelopmentExperts extends ExpertTools {

    public DevelopmentExperts(LLM llm) {
        super(llm);
    }

    public DevelopmentExperts() {
        super();
    }

    @RegisterTool(tags = {"documentation"})
    public String generateTechnicalDocumentation(String codeOrFeature) {
        /**
         * Generate technical documentation by consulting a senior technical writer.
         * This expert focuses on creating clear, comprehensive documentation for developers.
         *
         * @param codeOrFeature The code or feature to document
         * @return Comprehensive technical documentation
         */
        return promptExpert(
                """
                        You are a senior technical writer with 15 years of experience in software documentation.
                        You have particular expertise in:
                        - Writing clear and precise API documentation
                        - Explaining complex technical concepts to developers
                        - Documenting implementation details and integration points
                        - Creating code examples that illustrate key concepts
                        - Identifying and documenting important caveats and edge cases
                        
                        Your documentation is known for striking the perfect balance between completeness
                        and clarity. You understand that good technical documentation serves as both
                        a reference and a learning tool.
                        """,
                """
                        Please create comprehensive technical documentation for the following code or feature:
                        
                        %s
                        
                        Your documentation should include:
                        1. A clear overview of the feature's purpose and functionality
                        2. Detailed explanation of the implementation approach
                        3. Key interfaces and integration points
                        4. Usage examples with code snippets
                        5. Important considerations and edge cases
                        6. Performance implications if relevant
                        
                        Focus on providing information that developers need to effectively understand
                        and work with this code.
                        """.formatted(codeOrFeature)
        );
    }

    @RegisterTool(tags = {"testing"})
    public String designTestSuite(String featureDescription) {
        /**
         * Design a comprehensive test suite by consulting a senior QA engineer.
         * This expert focuses on creating thorough test coverage with attention to edge cases.
         *
         * @param featureDescription Description of the feature to test
         * @return Comprehensive test suite design
         */
        return promptExpert(
                """
                        You are a senior QA engineer with 12 years of experience in test design and automation.
                        Your expertise includes:
                        - Comprehensive test strategy development
                        - Unit, integration, and end-to-end testing
                        - Performance and stress testing
                        - Security testing considerations
                        - Test automation best practices
                        
                        You are particularly skilled at identifying edge cases and potential failure modes
                        that others might miss. Your test suites are known for their thoroughness and
                        their ability to catch issues early in the development cycle.
                        """,
                """
                        Please design a comprehensive test suite for the following feature:
                        
                        %s
                        
                        Your test design should cover:
                        1. Unit tests for individual components
                        2. Integration tests for component interactions
                        3. End-to-end tests for critical user paths
                        4. Performance test scenarios if relevant
                        5. Edge cases and error conditions
                        6. Test data requirements
                        
                        For each test category, provide:
                        - Specific test scenarios
                        - Expected outcomes
                        - Important edge cases to consider
                        - Potential testing challenges
                        """.formatted(featureDescription)
        );
    }

    @RegisterTool(tags = {"code_quality"})
    public String performCodeReview(String code) {
        /**
         * Review code and suggest improvements by consulting a senior software architect.
         * This expert focuses on code quality, architecture, and best practices.
         *
         * @param code The code to review
         * @return Detailed code review with improvement suggestions
         */
        return promptExpert(
                """
                        You are a senior software architect with 20 years of experience in code review
                        and software design. Your expertise includes:
                        - Software architecture and design patterns
                        - Code quality and maintainability
                        - Performance optimization
                        - Scalability considerations
                        - Security best practices
                        
                        You have a talent for identifying subtle design issues and suggesting practical
                        improvements that enhance code quality without over-engineering.
                        """,
                """
                        Please review the following code and provide detailed improvement suggestions:
                        
                        %s
                        
                        Consider and address:
                        1. Code organization and structure
                        2. Potential design pattern applications
                        3. Performance optimization opportunities
                        4. Error handling completeness
                        5. Edge case handling
                        6. Maintainability concerns
                        
                        For each suggestion:
                        - Explain the current issue
                        - Provide the rationale for change
                        - Suggest specific improvements
                        - Note any trade-offs to consider
                        """.formatted(code)
        );
    }

    @RegisterTool(tags = {"communication"})
    public String writeFeatureAnnouncement(String featureDetails, String audience) {
        /**
         * Write a feature announcement by consulting a product marketing expert.
         * This expert focuses on clear communication of technical features to different audiences.
         *
         * @param featureDetails Technical details of the feature
         * @param audience Target audience for the announcement (e.g., "technical", "business")
         * @return Feature announcement tailored to the specified audience
         */
        return promptExpert(
                """
                        You are a senior product marketing manager with 12 years of experience in
                        technical product communication. Your expertise includes:
                        - Translating technical features into clear value propositions
                        - Crafting compelling product narratives
                        - Adapting messaging for different audience types
                        - Building excitement while maintaining accuracy
                        - Creating clear calls to action
                        
                        You excel at finding the perfect balance between technical accuracy and
                        accessibility, ensuring your communications are both precise and engaging.
                        """,
                """
                        Please write a feature announcement for the following feature:
                        
                        %s
                        
                        This announcement is intended for a %s audience.
                        
                        Your announcement should include:
                        1. A compelling introduction
                        2. Clear explanation of the feature
                        3. Key benefits and use cases
                        4. Technical details (adapted to audience)
                        5. Implementation requirements
                        6. Next steps or call to action
                        
                        Ensure the tone and technical depth are appropriate for a %s audience.
                        Focus on conveying both the value and the practical implications of this feature.
                        """.formatted(featureDetails, audience, audience)
        );
    }

    public static void main(String[] args) {
        // Example usage of the DevelopmentExperts class
        DevelopmentExperts experts = new DevelopmentExperts();

        String code = """
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
                
        """;

        String featureDescription = "Conjure development experts with an LLM!.";

        String documentation = experts.generateTechnicalDocumentation(code);
        System.out.println("Documentation: " + documentation);

        String testSuite = experts.designTestSuite(featureDescription);
        System.out.println("Test Suite: " + testSuite);

        String codeReview = experts.performCodeReview(code);
        System.out.println("Code Review: " + codeReview);

        String announcement = experts.writeFeatureAnnouncement(featureDescription, "technical");
        System.out.println("Announcement: " + announcement);
    }
}
