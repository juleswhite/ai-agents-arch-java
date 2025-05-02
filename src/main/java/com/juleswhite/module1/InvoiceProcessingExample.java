package com.juleswhite.module1;

import java.util.List;

public class InvoiceProcessingExample {

    /*
     * This example demonstrates how to create an agent that processes invoices.
     * The agent will extract important information from the invoice text and store it.
     * It will also handle errors and provide confirmation of successful processing.
     */
    public static void main(String[] args) throws Exception {
        // Define our invoice processing goals
        List<Goal> goals = List.of(
                new Goal(
                        1,
                        "Persona",
                        "You are an Invoice Processing Agent, specialized in handling and storing invoice data."
                ),
                new Goal(
                        2,
                        "Process Invoices",
                        """
                        Your goal is to process invoices by extracting their data and storing it properly.
                        For each invoice:
                        1. Extract all important information including numbers, dates, amounts, and line items
                        2. Store the extracted data indexed by invoice number
                        3. Provide confirmation of successful processing
                        4. Handle any errors appropriately
                        """
                )
        );

        // Create an instance of InvoiceTools
        InvoiceTools invoiceTools = new InvoiceTools();

        // Create and run the agent with the tools instance
        Agent agent = Agents.createInstanceAgent(invoiceTools, goals);

        // Sample invoice to process
        String invoiceText = """
            ACME CORPORATION
            INVOICE #A12345
            
            Date: January 15, 2025
            
            Bill To:
            Globex Industries
            123 Business Ave
            Metropolis, NY 10001
            
            Item          Quantity    Unit Price    Total
            ---------------------------------------------
            Widget A      5           $29.99        $149.95
            Widget B      2           $49.99        $99.98
            Installation  1           $75.00        $75.00
            
            Subtotal:                              $324.93
            Tax (8%):                               $26.00
            Total:                                 $350.93
            
            Payment due within 30 days.
            Thank you for your business!
            """;

        // Run the agent with this invoice
        String initialPrompt = "Process this invoice: " + invoiceText;
        Agents.runAndPrintResults(agent, initialPrompt, 10);
    }
}
