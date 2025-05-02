package com.juleswhite.module1;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceExperts extends ExpertTools {

    private final String rulesPath = "config/purchasing_rules.txt";

    public InvoiceExperts(LLM llm) {
        super(llm);
    }

    public InvoiceExperts() {
        super();
    }

    @RegisterTool(tags = {"invoice_processing", "categorization"})
    public String categorizeExpenditure(String description) {
        /**
         * Categorize an invoice expenditure based on a short description.
         *
         * @param description A one-sentence summary of the expenditure.
         * @return A category name from the predefined set of 20 categories.
         */
        List<String> categories = List.of(
                "Office Supplies", "IT Equipment", "Software Licenses", "Consulting Services",
                "Travel Expenses", "Marketing", "Training & Development", "Facilities Maintenance",
                "Utilities", "Legal Services", "Insurance", "Medical Services", "Payroll",
                "Research & Development", "Manufacturing Supplies", "Construction", "Logistics",
                "Customer Support", "Security Services", "Miscellaneous"
        );

        return promptExpert(
                "A senior financial analyst with deep expertise in corporate spending categorization.",
                "Given the following description: '" + description + "', classify the expense into one of these categories:\n" + categories
        );
    }

    @RegisterTool(tags = {"invoice_processing", "validation"})
    public Map<String, Object> checkPurchasingRules(Map<String, Object> invoiceData) {
        /**
         * Validate an invoice against company purchasing policies, returning a structured response.
         *
         * @param invoiceData Extracted invoice details, including vendor, amount, and line items.
         * @return A structured JSON response indicating whether the invoice is compliant and why.
         */
        String purchasingRules;

        try {
            purchasingRules = new String(Files.readAllBytes(Paths.get(rulesPath)));
        } catch (IOException e) {
            purchasingRules = "No rules available. Assume all invoices are compliant.";
        }

        Map<String, Object> validationSchema = new HashMap<>();
        validationSchema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("compliant", Map.of("type", "boolean"));
        properties.put("issues", Map.of("type", "string"));

        validationSchema.put("properties", properties);

        // Using the promptLlmForJson method we created earlier
        return promptLlmForJson(
                validationSchema,
                """
                Given this invoice data: %s, check whether it complies with company purchasing rules.
                The latest purchasing rules are as follows:
                
                %s
                
                Respond with a JSON object containing:
                - `compliant`: true if the invoice follows all policies, false otherwise.
                - `issues`: A brief explanation of any violations or missing requirements.
                """.formatted(invoiceData, purchasingRules)
        );
    }


    @RegisterTool(tags = {"extraction"})
    public Map<String, Object> promptLlmForJson(Map<String, Object> schema, String prompt) {
        /**
         * Have the LLM generate JSON in response to a prompt. Always use this tool when you need structured data out of the LLM.
         * This method takes a JSON schema that specifies the structure of the expected JSON response.
         *
         * @param schema JSON schema defining the expected structure
         * @param prompt The prompt to send to the LLM
         * @return A map matching the provided schema with extracted information
         */
        // Access LLM function from environment
        ObjectMapper objectMapper = new ObjectMapper();

        // Try up to 3 times to get valid JSON
        for (int i = 0; i < 3; i++) {
            try {
                // Create the system message with schema instruction
                String systemPrompt = "You MUST produce output that adheres to the following JSON schema:\n\n" +
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema) +
                        ". Output your JSON in a ```json markdown block.";

                // Create the messages for the prompt
                List<Message> messages = new ArrayList<>();
                messages.add(new Message("system", systemPrompt));
                messages.add(new Message("user", prompt));

                // Create and send the prompt
                LLM.Prompt llmPrompt = new LLM.Prompt(messages, null);

                String response = this.llm.generateResponse(llmPrompt);

                // Check if the response has json inside of a markdown code block
                if (response.contains("```json")) {
                    // Search from the front and then the back
                    int start = response.indexOf("```json");
                    int end = response.lastIndexOf("```");
                    response = response.substring(start + 7, end).trim();
                }

                // Parse and validate the JSON response
                return objectMapper.readValue(response, Map.class);

            } catch (Exception e) {
                if (i == 2) {  // On last try, throw the error
                    throw new RuntimeException("Failed to generate valid JSON after 3 attempts", e);
                }
                System.out.println("Error generating response: " + e.getMessage());
                System.out.println("Retrying...");
            }
        }

        // This should never happen due to the exception in the loop
        return Map.of();
    }


    public static void main(String[] args){
        // Example usage
        InvoiceExperts experts = new InvoiceExperts();
        String description = "Purchase the new ACMEZING Unified 9.1.13 Proprietary " +
                "Prompting Solves All Problems Software Tool.";
        String category = experts.categorizeExpenditure(description);
        System.out.println("Category: " + category);

        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("vendor", "TechCorp");
        invoiceData.put("amount", 50000);
        invoiceData.put("line_items", List.of("50 ACMEZING Unified 9.1.13 Proprietary Prompting Software Licenses", "10 monitors"));

        Map<String, Object> validationResult = experts.checkPurchasingRules(invoiceData);
        System.out.println("Validation Result: " + validationResult);
    }

}
