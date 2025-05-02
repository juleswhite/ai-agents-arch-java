package com.juleswhite.module1;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.juleswhite.module1.LLM.Prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceTools {

    public static final String INVOICE_SCHEMA = """
            {
              "type": "object",
              "required": ["invoice_number", "date", "amount"],
              "properties": {
                "invoice_number": {
                  "type": "string"
                },
                "date": {
                  "type": "string",
                  "format": "date"
                },
                "amount": {
                  "type": "object",
                  "properties": {
                    "value": {
                      "type": "number"
                    },
                    "currency": {
                      "type": "string"
                    }
                  },
                  "required": ["value", "currency"]
                },
                "vendor": {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "tax_id": {
                      "type": "string"
                    },
                    "address": {
                      "type": "string"
                    }
                  },
                  "required": ["name"]
                },
                "line_items": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "description": {
                        "type": "string"
                      },
                      "quantity": {
                        "type": "number"
                      },
                      "unit_price": {
                        "type": "number"
                      },
                      "total": {
                        "type": "number"
                      }
                    },
                    "required": ["description", "total"]
                  }
                }
              }
            }
    """;

    // Private instance variables for state
    private Map<String, Map<String, Object>> invoiceStorage;
    private LLM llm;

    // Constructor that accepts the LLM instance
    public InvoiceTools(LLM llm) {
        this.llm = llm;
        this.invoiceStorage = new HashMap<>();
    }

    // Default constructor
    public InvoiceTools() {
        this(new LLM());
    }

    @RegisterTool(terminal = true)
    public void terminate(String message){
        System.out.println(message);
    }

    @RegisterTool(tags = {"storage", "invoices"},
            parameters = INVOICE_SCHEMA)
    public Map<String, Object> storeInvoice(Map<String, Object> invoiceData) {
        /**
         * Store an invoice in our invoice database. If an invoice with the same number
         * already exists, it will be updated.
         *
         * @param invoiceData The processed invoice data to store
         * @return A map containing the storage result and invoice number
         */
        // Get our invoice storage from instance state
        if (this.invoiceStorage == null) {
            this.invoiceStorage = new HashMap<>();
        }

        // Extract invoice number for reference
        String invoiceNumber = (String) invoiceData.get("invoice_number");
        if (invoiceNumber == null || invoiceNumber.isEmpty()) {
            throw new IllegalArgumentException("Invoice data must contain an invoice number");
        }

        // Store the invoice
        this.invoiceStorage.put(invoiceNumber, invoiceData);

        // Create result
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Stored invoice " + invoiceNumber);
        result.put("invoice_number", invoiceNumber);

        return result;
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
                Prompt llmPrompt = new Prompt(messages, null);

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


    @RegisterTool(tags = {"document_processing", "invoices"})
    public Map<String, Object> extractInvoiceData(String documentText) {
        /**
         * Extract standardized invoice data from document text. This tool enforces a consistent
         * schema for invoice data extraction across all documents.
         *
         * @param documentText The text content of the invoice to process
         * @return A map containing extracted invoice data in a standardized format
         */

        Map<String, Object> invoiceSchema = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            invoiceSchema = objectMapper.readValue(INVOICE_SCHEMA, Map.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create invoice schema", e);
        }

        // Create a focused prompt that guides the LLM in invoice extraction
        String extractionPrompt = """
                Extract invoice information from the following document text. 
                Focus on identifying:
                - Invoice number (usually labeled as 'Invoice #', 'Reference', etc.)
                - Date (any dates labeled as 'Invoice Date', 'Issue Date', etc.)
                - Amount (total amount due, including currency)
                - Vendor information (company name, tax ID if present, address)
                - Line items (individual charges and their details)
                
                Document text:
                %s
                """.formatted(documentText);

        // Use our general extraction tool with the specialized schema and prompt
        return promptLlmForJson(
                invoiceSchema,
                extractionPrompt
        );
    }


    public static void main(String[] args) {
        // Example usage
        LLM llm = new LLM(); // Assuming LLM is properly initialized
        InvoiceTools invoiceTools = new InvoiceTools(llm);

        String documentText = "Sample invoice text here...";
        Map<String, Object> extractedData = invoiceTools.extractInvoiceData(documentText);

        System.out.println("Extracted Invoice Data: " + extractedData);
    }
}
