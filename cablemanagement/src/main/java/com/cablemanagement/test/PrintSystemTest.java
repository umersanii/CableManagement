package com.cablemanagement.test;

import com.cablemanagement.invoice.InvoiceData;
import com.cablemanagement.invoice.Item;
import com.cablemanagement.invoice.PrintManager;
import com.cablemanagement.invoice.InvoiceGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the print system functionality
 * Tests the new print preview feature for sales invoices
 */
public class PrintSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== Print System Test ===");
        testPDFGeneration();
        testPrintPreviewFunctionality();
    }
    
    /**
     * Test PDF generation to make sure PDFs are created properly
     */
    public static void testPDFGeneration() {
        System.out.println("\n=== Testing PDF Generation ===");
        
        try {
            // Create sample invoice data
            List<Item> items = new ArrayList<>();
            items.add(new Item("Test Cable", 1, 10.00, 0.0));
            
            InvoiceData testInvoice = new InvoiceData(
                "TEST-001",
                "2025-07-25",
                "Test Customer",
                "Test Address",
                0.0,
                items
            );
            
            // Test PDF generation
            String testFileName = System.getProperty("java.io.tmpdir") + File.separator + "test_invoice.pdf";
            System.out.println("Generating test PDF: " + testFileName);
            
            InvoiceGenerator.generatePDF(testInvoice, testFileName);
            
            // Check if file was created
            File pdfFile = new File(testFileName);
            if (pdfFile.exists() && pdfFile.length() > 0) {
                System.out.println("✓ PDF generated successfully: " + pdfFile.length() + " bytes");
                // Clean up test file
                pdfFile.delete();
            } else {
                System.out.println("✗ PDF generation failed or file is empty");
            }
            
        } catch (Exception e) {
            System.err.println("✗ PDF generation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test the new print preview functionality
     * This simulates creating a sales invoice and opening it for print preview
     */
    public static void testPrintPreviewFunctionality() {
        try {
            System.out.println("\n=== Testing Print Preview Functionality ===");
            
            // Create sample invoice data
            List<Item> items = new ArrayList<>();
            items.add(new Item("Cable Type A", 10, 5.50, 0.0));
            items.add(new Item("Cable Type B", 5, 12.00, 5.0));
            items.add(new Item("Connector Set", 2, 25.00, 0.0));
            
            InvoiceData testInvoice = new InvoiceData(
                "SI-2025-001",
                "2025-07-25",
                "Test Customer Ltd",
                "123 Test Street, Test City, TC 12345",
                0.0,
                items
            );
            
            System.out.println("Test invoice created:");
            System.out.println("Invoice Number: " + testInvoice.getInvoiceNumber());
            System.out.println("Customer: " + testInvoice.getCustomerName());
            System.out.println("Date: " + testInvoice.getDate());
            System.out.println("Items count: " + testInvoice.getItems().size());
            
            // Test the print preview method (this should open the PDF in Edge/default viewer)
            System.out.println("\n=== Testing Print Preview Method ===");
            System.out.println("This should open the PDF in your default viewer (Edge)...");
            
            boolean previewResult = PrintManager.openInvoiceForPrintPreview(testInvoice, "Sales");
            System.out.println("Print preview result: " + (previewResult ? "SUCCESS" : "FAILED"));
            
            if (previewResult) {
                System.out.println("✓ PDF should now be open in Microsoft Edge or your default PDF viewer");
                System.out.println("✓ You should be able to press Ctrl+P to print the invoice");
            } else {
                System.out.println("✗ Print preview failed - check error messages above");
            }
            
            System.out.println("\n=== Test Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}