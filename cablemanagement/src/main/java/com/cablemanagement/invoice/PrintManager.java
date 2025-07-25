package com.cablemanagement.invoice;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Centralized print management for all invoice-related printing operations
 */
public class PrintManager {
    
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Print an invoice with user confirmation
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice (e.g., "Sales", "Purchase", "Return")
     * @return true if printing was successful
     */
    public static boolean printInvoice(InvoiceData invoiceData, String invoiceType) {
        return printInvoice(invoiceData, invoiceType, true);
    }
    
    /**
     * Print an invoice with optional user confirmation
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice (e.g., "Sales", "Purchase", "Return")
     * @param showConfirmation Whether to show confirmation dialog
     * @return true if printing was successful
     */
    public static boolean printInvoice(InvoiceData invoiceData, String invoiceType, boolean showConfirmation) {
        try {
            if (showConfirmation) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Print Invoice");
                confirmAlert.setHeaderText("Print " + invoiceType + " Invoice");
                confirmAlert.setContentText("Do you want to print Invoice #" + invoiceData.getInvoiceNumber() + "?");
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return false;
                }
            }
            
            // Generate temporary filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate and print
            boolean success = InvoiceGenerator.generateAndPrint(invoiceData, filename);
            
            if (success) {
                showSuccessAlert("Print Successful", 
                    invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + " has been sent to the printer.");
            } else {
                showErrorAlert("Print Failed", 
                    "Failed to print " + invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    ". Please check your printer connection and try again.");
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Print Error", "An error occurred while printing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print with printer selection dialog
     * @param invoiceData The invoice data to print
     * @param invoiceType The type of invoice
     * @return true if printing was successful
     */
    public static boolean printInvoiceWithPrinterSelection(InvoiceData invoiceData, String invoiceType) {
        try {
            // Get available printers
            String[] printers = InvoiceGenerator.getAvailablePrinters();
            
            if (printers.length == 0) {
                showErrorAlert("No Printers", "No printers are available. Please check your printer installation.");
                return false;
            }
            
            // Show printer selection dialog
            ChoiceDialog<String> printerDialog = new ChoiceDialog<>(printers[0], printers);
            printerDialog.setTitle("Select Printer");
            printerDialog.setHeaderText("Print " + invoiceType + " Invoice");
            printerDialog.setContentText("Choose a printer for Invoice #" + invoiceData.getInvoiceNumber() + ":");
            
            Optional<String> selectedPrinter = printerDialog.showAndWait();
            if (selectedPrinter.isEmpty()) {
                return false;
            }
            
            // Generate temporary filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate PDF first
            InvoiceGenerator.generatePDF(invoiceData, filename);
            
            // Print to selected printer
            boolean success = InvoiceGenerator.printToSpecificPrinter(filename, selectedPrinter.get());
            
            if (success) {
                showSuccessAlert("Print Successful", 
                    invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    " has been sent to printer: " + selectedPrinter.get());
            } else {
                showErrorAlert("Print Failed", 
                    "Failed to print " + invoiceType + " Invoice #" + invoiceData.getInvoiceNumber() + 
                    " to printer: " + selectedPrinter.get());
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Print Error", "An error occurred while printing: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print multiple invoices in batch
     * @param invoiceDataList List of invoice data to print
     * @param invoiceType The type of invoices
     * @return Number of successfully printed invoices
     */
    public static int printInvoicesBatch(List<InvoiceData> invoiceDataList, String invoiceType) {
        if (invoiceDataList.isEmpty()) {
            showErrorAlert("No Data", "No invoices to print.");
            return 0;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Batch Print");
        confirmAlert.setHeaderText("Print Multiple " + invoiceType + " Invoices");
        confirmAlert.setContentText("Do you want to print " + invoiceDataList.size() + " " + invoiceType + " invoices?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return 0;
        }
        
        int successCount = 0;
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        for (int i = 0; i < invoiceDataList.size(); i++) {
            InvoiceData invoiceData = invoiceDataList.get(i);
            
            try {
                String filename = TEMP_DIR + File.separator + invoiceType + "_Invoice_" + 
                                 invoiceData.getInvoiceNumber() + "_" + timestamp + "_" + (i + 1) + ".pdf";
                
                if (InvoiceGenerator.generateAndPrint(invoiceData, filename)) {
                    successCount++;
                }
                
                // Small delay between prints to avoid overwhelming the printer
                Thread.sleep(500);
                
            } catch (Exception e) {
                System.err.println("Failed to print invoice " + invoiceData.getInvoiceNumber() + ": " + e.getMessage());
            }
        }
        
        if (successCount == invoiceDataList.size()) {
            showSuccessAlert("Batch Print Successful", 
                "All " + successCount + " " + invoiceType + " invoices have been sent to the printer.");
        } else {
            showErrorAlert("Partial Print Success", 
                successCount + " out of " + invoiceDataList.size() + " " + invoiceType + 
                " invoices were printed successfully.");
        }
        
        return successCount;
    }
    
    /**
     * Preview and then print an invoice
     * @param invoiceData The invoice data
     * @param invoiceType The type of invoice
     * @return true if printing was successful
     */
    public static boolean previewAndPrint(InvoiceData invoiceData, String invoiceType) {
        try {
            // Generate temporary filename for preview
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = TEMP_DIR + File.separator + invoiceType + "_Preview_" + 
                             invoiceData.getInvoiceNumber() + "_" + timestamp + ".pdf";
            
            // Generate PDF for preview
            InvoiceGenerator.generatePDF(invoiceData, filename);
            
            // Show preview dialog
            Alert previewAlert = new Alert(Alert.AlertType.CONFIRMATION);
            previewAlert.setTitle("Print Preview");
            previewAlert.setHeaderText("Preview " + invoiceType + " Invoice #" + invoiceData.getInvoiceNumber());
            previewAlert.setContentText("PDF has been generated. Do you want to proceed with printing?\n\n" +
                                       "File location: " + filename);
            
            Optional<ButtonType> result = previewAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return InvoiceGenerator.printPDF(filename);
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Preview Error", "An error occurred while generating preview: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get printer status information
     * @return String containing printer status
     */
    public static String getPrinterStatus() {
        try {
            String[] printers = InvoiceGenerator.getAvailablePrinters();
            if (printers.length == 0) {
                return "No printers available";
            }
            
            StringBuilder status = new StringBuilder();
            status.append("Available Printers (").append(printers.length).append("):\n");
            for (String printer : printers) {
                status.append("• ").append(printer).append("\n");
            }
            
            return status.toString().trim();
            
        } catch (Exception e) {
            return "Error getting printer status: " + e.getMessage();
        }
    }
    
    /**
     * Clean up temporary print files older than 24 hours
     */
    public static void cleanupTempFiles() {
        try {
            File tempDir = new File(TEMP_DIR);
            File[] files = tempDir.listFiles((dir, name) -> 
                name.contains("Invoice_") && name.endsWith(".pdf"));
            
            if (files != null) {
                long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
                int deletedCount = 0;
                
                for (File file : files) {
                    if (file.lastModified() < oneDayAgo) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
                
                if (deletedCount > 0) {
                    System.out.println("Cleaned up " + deletedCount + " temporary print files.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error cleaning up temporary files: " + e.getMessage());
        }
    }
    
    // Helper methods for showing alerts
    private static void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
