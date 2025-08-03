package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

public class InvoiceGenerator {
    
    /**
     * Get the appropriate invoice title based on the invoice type
     * @param data The invoice data containing the type
     * @return The formatted invoice title
     */
    private static String getInvoiceTitle(InvoiceData data) {
        String type = data.getType();
        if (type == null) {
            return "Invoice";
        }
        
        switch (type.toLowerCase()) {
            case "purchase":
                return "Purchase Invoice";
            case "purchase_return":
                return "Purchase Return Invoice";
            case "sale":
                return "Sales Invoice";
            case "sale_return":
                return "Sales Return Invoice";
            case "raw_stock":
                return "Stock Usage Invoice";
            case "production":
                return "Production Invoice";
            case "production_return":
                return "Production Return Invoice";
            default:
                return "Invoice";
        }
    }
    public static void generatePDF(InvoiceData data, String filename) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            // Logo (Optional - only add if logo file exists)
            try {
                // Try multiple possible logo locations
                String[] logoPaths = {
                    "src/main/resources/LOGO.jpg",
                    "CableManagement/cablemanagement/src/main/resources/LOGO.jpg",
                    "CableManagement/cablemanagement/LOGO.jpg",
                    InvoiceGenerator.class.getResource("/LOGO.jpg").getPath()
                };
                
                boolean logoFound = false;
                for (String logoPath : logoPaths) {
                    try {
                        File logoFile = new File(logoPath);
                        if (logoFile.exists() && logoFile.length() > 0) {
                            Image logo = Image.getInstance(logoPath);
                            logo.scaleToFit(100, 100);
                            logo.setAlignment(Element.ALIGN_CENTER);
                            document.add(logo);
                            System.out.println("Logo loaded successfully from: " + logoPath);
                            logoFound = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Continue to next path if this one fails
                        continue;
                    }
                }
                
                if (!logoFound) {
                    System.out.println("Logo file not found in any of the expected locations - proceeding without logo");
                }
            } catch (Exception logoEx) {
                System.out.println("Could not load logo: " + logoEx.getMessage() + " - proceeding without logo");
            }

            // Header
            Paragraph header = new Paragraph("HASEEB WIRE & CABLES", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            // Dynamic invoice title based on invoice type
            String invoiceTitle = getInvoiceTitle(data);
            Paragraph subHeader = new Paragraph(invoiceTitle, headerFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            Paragraph contact = new Paragraph("Khalil Abad, Amangarh, Nowshera\n0333-4100520 / 0333-9260587\n", regularFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);
            document.add(new Chunk(new DottedLineSeparator()));
            document.add(Chunk.NEWLINE);

            // Customer/Supplier Info & Invoice Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{6f, 4f});

            // Dynamic entity info based on invoice type with enhanced metadata
            StringBuilder entityInfo = new StringBuilder();
            
            // Handle different invoice types
            if (data.getType().toLowerCase().equals(InvoiceData.TYPE_RAW_STOCK)) {
                // For Raw Stock Usage, display reference/purpose
                if (data.hasMetadata("reference")) {
                    entityInfo.append("Reference/Purpose: ").append(data.getMetadata("reference"));
                }
            } else if (data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION) ||
                      data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION_RETURN)) {
                // For Production Invoice, display notes
                if (data.hasMetadata("notes")) {
                    entityInfo.append("Notes: ").append(data.getMetadata("notes"));
                }
            } else {
                // For other invoice types (purchase/sales)
                String entityLabel = data.getType().toLowerCase().contains("purchase") ? "Supplier" : "Customer";
                
                // Only display entity name without the label for suppliers as requested
                if (data.getType().toLowerCase().contains("purchase")) {
                    entityInfo.append(data.getEntityName());
                } else {
                    entityInfo.append(entityLabel).append(": ").append(data.getEntityName());
                }
                
                // Skip adding the address as per client request
                
                // Add tehsil if available in metadata
                if (data.hasMetadata("tehsil") && !data.getMetadata("tehsil").toString().isEmpty()) {
                    entityInfo.append("\nTehsil: ").append(data.getMetadata("tehsil"));
                }
                
                // Add contact if available in metadata
                if (data.hasMetadata("contact") && !data.getMetadata("contact").toString().isEmpty()) {
                    entityInfo.append("\nContact: ").append(data.getMetadata("contact"));
                }
            }
            
            PdfPCell entityCell = new PdfPCell(new Phrase(entityInfo.toString(), regularFont));
            entityCell.setBorder(Rectangle.NO_BORDER);

            // Build invoice cell content based on type
            StringBuilder invoiceCellContent = new StringBuilder();
            invoiceCellContent.append("Invoice #: ").append(data.getInvoiceNumber()).append("\n");
            invoiceCellContent.append("Date: ").append(data.getDate()).append("\n");
            if (data.getType().toLowerCase().contains("return")) {
                invoiceCellContent.append("Original Invoice #: ").append(data.getOriginalInvoiceNumber()).append("\n");
            }
            invoiceCellContent.append("Operator: ").append(data.getOperator());

            PdfPCell invoiceCell = new PdfPCell(new Phrase(invoiceCellContent.toString(), regularFont));
            invoiceCell.setBorder(Rectangle.NO_BORDER);

            infoTable.addCell(entityCell);
            infoTable.addCell(invoiceCell);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Item Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2, 3, 2, 3});

            String[] headers = {"#", "Item", "Qty", "Unit Price", "Discount %", "Net Price"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            double total = 0;
            List<Item> items = data.getItems();
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                double amount = item.getUnitPrice() * item.getQuantity();
                double discount = amount * item.getDiscountPercent() / 100.0;
                double net = amount - discount;
                total += net;

                table.addCell(new Phrase(String.valueOf(i + 1), regularFont));
                table.addCell(new Phrase(item.getName(), regularFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), regularFont));
                table.addCell(new Phrase(String.format("%.2f", item.getUnitPrice()), regularFont));
                table.addCell(new Phrase(String.format("%.1f%%", item.getDiscountPercent()), regularFont));
                table.addCell(new Phrase(String.format("%.2f", net), regularFont));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Summary Table - handling different for raw stock vs other invoice types
            double totalDiscount = items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice() * i.getDiscountPercent() / 100.0).sum();
            double totalBalance = total + data.getPreviousBalance();
            int totalQuantity = items.stream().mapToInt(Item::getQuantity).sum();

            PdfPTable summaryHeadingTable = new PdfPTable(1);
            summaryHeadingTable.setWidthPercentage(100);

            PdfPCell summaryHeading = new PdfPCell(new Phrase("Invoice Summary", headerFont));
            summaryHeading.setBackgroundColor(BaseColor.LIGHT_GRAY);
            summaryHeading.setHorizontalAlignment(Element.ALIGN_CENTER);
            summaryHeading.setPadding(5);
            summaryHeading.setBorder(Rectangle.BOX);

            summaryHeadingTable.addCell(summaryHeading);
            document.add(summaryHeadingTable);

            // Different summary tables based on invoice type
            PdfPTable summary;
            
            if (data.getType().toLowerCase().equals(InvoiceData.TYPE_RAW_STOCK)) {
                // Simplified summary table for Raw Stock Usage
                summary = new PdfPTable(2);
                summary.setWidthPercentage(100);
                summary.setWidths(new float[]{5f, 5f});
                summary.setSpacingBefore(10f);
                
                // Get total amount from metadata if available, otherwise calculate
                Object totalAmountObj = data.hasMetadata("totalAmount") ? data.getMetadata("totalAmount") : total;
                double totalAmount = totalAmountObj instanceof Number ? ((Number)totalAmountObj).doubleValue() : total;

                summary.addCell(new Phrase("Total Quantity:", regularFont));
                summary.addCell(new Phrase(String.valueOf(totalQuantity), regularFont));
                
                summary.addCell(new Phrase("Total Usage Amount:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", totalAmount), regularFont));
            } else if (data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION) ||
                      data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION_RETURN)) {
                // Simplified summary table for Production Invoices
                summary = new PdfPTable(2);
                summary.setWidthPercentage(100);
                summary.setWidths(new float[]{5f, 5f});
                summary.setSpacingBefore(10f);
                
                summary.addCell(new Phrase("Total Production Quantity:", regularFont));
                summary.addCell(new Phrase(String.valueOf(totalQuantity), regularFont));
            } else {
                // Regular summary table for purchase/sales invoices
                summary = new PdfPTable(4);
                summary.setWidthPercentage(100);
                summary.setWidths(new float[]{3f, 3f, 3f, 3f});
                summary.setSpacingBefore(10f);
                
                summary.addCell(new Phrase("Bill:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", total + totalDiscount), regularFont));
                summary.addCell(new Phrase("Discount:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", totalDiscount), regularFont));
                
                summary.addCell(new Phrase("Current Net Bill:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", total), regularFont));
                summary.addCell(new Phrase("Previous Balance:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", data.getPreviousBalance()), regularFont));
                
                summary.addCell(new Phrase("Total Quantity:", regularFont));
                summary.addCell(new Phrase(String.valueOf(totalQuantity), regularFont));
                summary.addCell(new Phrase("Total Balance:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", totalBalance), regularFont));
                
                summary.addCell(new Phrase("Paid:", regularFont));
                summary.addCell(new Phrase("0.00", regularFont));
                summary.addCell(new Phrase("Net Balance:", regularFont));
                summary.addCell(new Phrase(String.format("%.2f", totalBalance), regularFont));
            }
            
            document.add(summary);
            document.add(Chunk.NEWLINE);

            // Signature Section
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);

            PdfPCell leftSig = new PdfPCell(new Phrase("Signature: ____________________", regularFont));
            leftSig.setBorder(Rectangle.NO_BORDER);
            leftSig.setHorizontalAlignment(Element.ALIGN_LEFT);

            // Adjust the right signature label based on invoice type
            String rightSignatureLabel = "Supplier Signature: ____________________";
            if (data.getType().toLowerCase().equals(InvoiceData.TYPE_RAW_STOCK)) {
                rightSignatureLabel = "Approved By: ____________________";
            } else if (data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION) ||
                      data.getType().toLowerCase().equals(InvoiceData.TYPE_PRODUCTION_RETURN)) {
                rightSignatureLabel = "Approved By: ____________________";
            } else if (data.getType().toLowerCase().contains("sale")) {
                rightSignatureLabel = "Customer Signature: ____________________";
            }
            
            PdfPCell rightSig = new PdfPCell(new Phrase(rightSignatureLabel, regularFont));
            rightSig.setBorder(Rectangle.NO_BORDER);
            rightSig.setHorizontalAlignment(Element.ALIGN_RIGHT);

            signatureTable.addCell(leftSig);
            signatureTable.addCell(rightSig);
            document.add(signatureTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Paragraph thankYou = new Paragraph("THANK YOU!", headerFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            document.close();
            System.out.println("Invoice generated successfully: " + filename);
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Print a PDF file to the default printer
     * @param filename The path to the PDF file to print
     * @return true if printing was successful, false otherwise
     */
    public static boolean printPDF(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("File not found: " + filename);
                return false;
            }
            
            // Try using javax.print directly instead of Desktop API
            return printWithJavaxPrint(filename);
            
        } catch (Exception e) {
            System.err.println("Failed to print PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Print using javax.print API as fallback
     * @param filename The path to the PDF file to print
     * @return true if printing was successful, false otherwise
     */
    private static boolean printWithJavaxPrint(String filename) {
        try {
            // Get the default print service
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrintService == null) {
                System.err.println("No default printer found");
                return false;
            }
            
            // Create print job
            DocPrintJob printJob = defaultPrintService.createPrintJob();
            
            // Create print request attributes
            PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
            printAttributes.add(new Copies(1));
            printAttributes.add(MediaSizeName.ISO_A4);
            printAttributes.add(OrientationRequested.PORTRAIT);
            
            // Create document
            File file = new File(filename);
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc document = new SimpleDoc(new java.io.FileInputStream(file), flavor, null);
            
            // Print the document
            printJob.print(document, printAttributes);
            System.out.println("Document sent to printer using javax.print: " + filename);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to print using javax.print: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate and immediately print an invoice
     * @param data The invoice data
     * @param filename The temporary filename for the PDF
     * @return true if generation and printing were successful
     */
    public static String generateAndGetPath(InvoiceData data, String filename) {
        try {
            generatePDF(data, filename);
            System.out.println("PDF generated successfully: " + new File(filename).length() + " bytes");
            return filename;
        } catch (Exception e) {
            System.err.println("Failed to generate invoice: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Print the invoice without preview
     * @param data The invoice data
     * @param filename The file to save and print
     * @return true if successful
     */
    public static boolean generateAndPrint(InvoiceData data, String filename) {
        try {
            String pdfPath = generateAndGetPath(data, filename);
            if (pdfPath == null) {
                return false;
            }

            // Print in a separate thread but with proper synchronization
            final boolean[] printComplete = { false };
            final boolean[] printSuccess = { false };
            
            Thread printThread = new Thread(() -> {
                try {
                    boolean result = printWithJavaxPrint(pdfPath);
                    synchronized (printComplete) {
                        printSuccess[0] = result;
                        printComplete[0] = true;
                        printComplete.notify();
                    }
                } catch (Exception e) {
                    System.err.println("Print thread error: " + e.getMessage());
                    synchronized (printComplete) {
                        printComplete[0] = true;
                        printComplete.notify();
                    }
                }
            });
            printThread.setDaemon(true);
            printThread.start();

            // Wait for printing to complete with a timeout
            synchronized (printComplete) {
                if (!printComplete[0]) {
                    try {
                        printComplete.wait(5000); // 5 second timeout
                    } catch (InterruptedException e) {
                        System.err.println("Print wait interrupted");
                    }
                }
            }

            // If printing didn't complete in time, consider it failed but don't block
            if (!printComplete[0]) {
                System.err.println("Print operation timed out");
                return false;
            }

            return printSuccess[0];
        } catch (Exception e) {
            System.err.println("Failed to generate and print invoice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show available printers to the user
     * @return Array of available printer names
     */
    public static String[] getAvailablePrinters() {
        try {
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            String[] printerNames = new String[printServices.length];
            for (int i = 0; i < printServices.length; i++) {
                printerNames[i] = printServices[i].getName();
            }
            return printerNames;
        } catch (Exception e) {
            System.err.println("Failed to get available printers: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Preview the PDF using system's default PDF viewer
     * @param pdfPath Path to the PDF file
     * @return true if the command was launched successfully
     */
    public static boolean previewPDF(String pdfPath) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (osName.contains("linux")) {
                // Try xdg-open first
                pb = new ProcessBuilder("xdg-open", pdfPath);
            } else if (osName.contains("windows")) {
                pb = new ProcessBuilder("cmd", "/c", "start", pdfPath);
            } else if (osName.contains("mac")) {
                pb = new ProcessBuilder("open", pdfPath);
            } else {
                System.err.println("Unsupported operating system for preview");
                return false;
            }

            // Start the process and don't wait for it
            pb.start();
            
            System.out.println("PDF viewer command launched for: " + pdfPath);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to launch PDF viewer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate a PDF and open it for preview
     * @param data The invoice data
     * @param filename The file to save
     * @return true if generation and preview launch were successful
     */
    public static boolean generateAndPreview(InvoiceData data, String filename) {
        String pdfPath = generateAndGetPath(data, filename);
        if (pdfPath == null) {
            return false;
        }
        return previewPDF(pdfPath);
    }
    
    /**
     * Print to a specific printer
     * @param filename The path to the PDF file to print
     * @param printerName The name of the printer to use
     * @return true if printing was successful
     */
    public static boolean printToSpecificPrinter(String filename, String printerName) {
        try {
            // Verify file exists
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("PDF file does not exist: " + filename);
                return false;
            }
            
            System.out.println("Attempting to print PDF: " + filename + " to printer: " + printerName);
            
            // Find the specified printer
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            PrintService targetPrinter = null;
            
            System.out.println("Available printers:");
            for (PrintService service : printServices) {
                System.out.println("  - " + service.getName());
                if (service.getName().equals(printerName)) {
                    targetPrinter = service;
                }
            }
            
            if (targetPrinter == null) {
                System.err.println("Printer not found: " + printerName);
                return false;
            }
            
            // Check if printer supports PDF
            DocFlavor[] supportedFlavors = targetPrinter.getSupportedDocFlavors();
            boolean supportsPDF = false;
            for (DocFlavor flavor : supportedFlavors) {
                if (flavor.equals(DocFlavor.INPUT_STREAM.PDF)) {
                    supportsPDF = true;
                    break;
                }
            }
            
            System.out.println("Printer '" + printerName + "' supports PDF: " + supportsPDF);
            
            // Create print job for specific printer
            DocPrintJob printJob = targetPrinter.createPrintJob();
            
            // Create print request attributes
            PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
            printAttributes.add(new Copies(1));
            printAttributes.add(MediaSizeName.ISO_A4);
            printAttributes.add(OrientationRequested.PORTRAIT);
            
            // Create document with proper PDF flavor
            DocFlavor flavor = supportsPDF ? DocFlavor.INPUT_STREAM.PDF : DocFlavor.INPUT_STREAM.AUTOSENSE;
            FileInputStream fis = new FileInputStream(file);
            Doc document = new SimpleDoc(fis, flavor, null);
            
            // Print the document with print event listener
            PrintJobListener printJobListener = new PrintJobListener() {
                @Override
                public void printDataTransferCompleted(PrintJobEvent pje) {
                    System.out.println("Print data transfer completed successfully");
                }
                
                @Override
                public void printJobCompleted(PrintJobEvent pje) {
                    System.out.println("Print job completed successfully");
                }
                
                @Override
                public void printJobFailed(PrintJobEvent pje) {
                    System.err.println("Print job failed");
                }
                
                @Override
                public void printJobCanceled(PrintJobEvent pje) {
                    System.out.println("Print job was canceled");
                }
                
                @Override
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    System.out.println("No more print events");
                }
                
                @Override
                public void printJobRequiresAttention(PrintJobEvent pje) {
                    System.out.println("Print job requires attention");
                }
            };
            
            printJob.addPrintJobListener(printJobListener);
            printJob.print(document, printAttributes);
            
            // Wait a moment for the print job to be processed
            Thread.sleep(1000);
            
            System.out.println("Document sent to printer '" + printerName + "': " + filename);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to print to specific printer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
