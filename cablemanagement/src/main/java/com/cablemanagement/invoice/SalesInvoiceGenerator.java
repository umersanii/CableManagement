package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

import java.io.FileOutputStream;
import java.util.List;

public class SalesInvoiceGenerator {
    
    /**
     * Generate a properly formatted Sales Invoice PDF
     * @param data The invoice data containing customer and item details
     * @param filename The output filename for the PDF
     */
    public static void generateSalesInvoicePDF(InvoiceData data, String filename) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

            // Logo (Optional - comment out if no logo)
            try {
                Image logo = Image.getInstance("CableManagement/cablemanagement/src/main/java/com/cablemanagement/invoice/logo.png");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found, continuing without logo");
            }

            // Header
            Paragraph header = new Paragraph("HASEEB WIRE & CABLES", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Sales Invoice", headerFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(5);
            document.add(subHeader);

            Paragraph contact = new Paragraph("Khalil Abad, Amangarh, Nowshera\n0333-4100520 / 0333-9260587\n", regularFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            contact.setSpacingAfter(10);
            document.add(contact);
            
            document.add(new Chunk(new DottedLineSeparator()));
            document.add(Chunk.NEWLINE);

            // Customer & Invoice Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{6f, 4f});

            // Extract customer name from first item if it contains customer info
            String customerName = data.getCustomerName();
            String customerAddress = data.getCustomerAddress();
            
            // If customer info is in the item name, extract it
            if (!data.getItems().isEmpty() && customerName.equals("General Report")) {
                String firstItemName = data.getItems().get(0).getName();
                if (firstItemName.contains(" (Invoice: ")) {
                    customerName = firstItemName.substring(0, firstItemName.indexOf(" (Invoice: "));
                    customerAddress = "Customer Address"; // Default if not available
                }
            }

            PdfPCell customerCell = new PdfPCell(new Phrase(
                    "Bill To:\n" + customerName + "\n" +
                    "Address: " + customerAddress, regularFont));
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.setPaddingBottom(10);

            PdfPCell invoiceCell = new PdfPCell(new Phrase(
                    "Invoice #: " + data.getInvoiceNumber() + "\n" +
                    "Date: " + data.getDate() + "\n" +
                    "Sales Representative: Admin\n" +
                    "Payment Terms: Net 30", regularFont));
            invoiceCell.setBorder(Rectangle.NO_BORDER);
            invoiceCell.setPaddingBottom(10);

            infoTable.addCell(customerCell);
            infoTable.addCell(invoiceCell);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Item Table Header
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 4, 2, 2.5f, 2, 2.5f});

            String[] headers = {"#", "Description", "Qty", "Unit Price", "Discount", "Net Amount"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Add items to table
            double subtotal = 0;
            double totalDiscount = 0;
            List<Item> items = data.getItems();
            
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                
                // Clean up item name (remove customer info if present)
                String itemName = item.getName();
                if (itemName.contains(" (Invoice: ")) {
                    itemName = "Sales Transaction - " + itemName.substring(itemName.indexOf(" (Invoice: ") + 11).replace(")", "");
                }
                
                double amount = item.getUnitPrice() * item.getQuantity();
                double discount = amount * item.getDiscountPercent() / 100.0;
                double net = amount - discount;
                
                subtotal += amount;
                totalDiscount += discount;

                // Row cells
                table.addCell(new Phrase(String.valueOf(i + 1), regularFont));
                table.addCell(new Phrase(itemName, regularFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), regularFont));
                table.addCell(new Phrase(String.format("$%.2f", item.getUnitPrice()), regularFont));
                table.addCell(new Phrase(String.format("$%.2f", discount), regularFont));
                table.addCell(new Phrase(String.format("$%.2f", net), regularFont));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Summary Section
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            double total = subtotal - totalDiscount;
            double grandTotal = total + data.getPreviousBalance();

            // Summary rows
            addSummaryRow(summaryTable, "Subtotal:", String.format("$%.2f", subtotal), regularFont, boldFont);
            addSummaryRow(summaryTable, "Total Discount:", String.format("$%.2f", totalDiscount), regularFont, boldFont);
            addSummaryRow(summaryTable, "Net Amount:", String.format("$%.2f", total), regularFont, boldFont);
            
            if (data.getPreviousBalance() != 0) {
                addSummaryRow(summaryTable, "Previous Balance:", String.format("$%.2f", data.getPreviousBalance()), regularFont, boldFont);
                addSummaryRow(summaryTable, "Grand Total:", String.format("$%.2f", grandTotal), boldFont, boldFont);
            }

            document.add(summaryTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Payment Information
            Paragraph paymentInfo = new Paragraph("Payment Information:\n" +
                "• Payment due within 30 days of invoice date\n" +
                "• Please include invoice number on your payment\n" +
                "• Late payments may incur additional charges", regularFont);
            paymentInfo.setSpacingBefore(15);
            document.add(paymentInfo);
            document.add(Chunk.NEWLINE);

            // Signature Section
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(20);

            PdfPCell leftSig = new PdfPCell(new Phrase("Customer Signature: ____________________", regularFont));
            leftSig.setBorder(Rectangle.NO_BORDER);
            leftSig.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell rightSig = new PdfPCell(new Phrase("Authorized Signature: ____________________", regularFont));
            rightSig.setBorder(Rectangle.NO_BORDER);
            rightSig.setHorizontalAlignment(Element.ALIGN_RIGHT);

            signatureTable.addCell(leftSig);
            signatureTable.addCell(rightSig);
            document.add(signatureTable);

            // Footer
            document.add(Chunk.NEWLINE);
            Paragraph thankYou = new Paragraph("THANK YOU FOR YOUR BUSINESS!", headerFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(20);
            document.add(thankYou);

            document.close();
            System.out.println("Sales Invoice PDF generated: " + filename);
            
        } catch (Exception e) {
            System.err.println("Error generating Sales Invoice PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a properly formatted Return Sales Invoice PDF
     * @param data The invoice data containing return details
     * @param filename The output filename for the PDF
     */
    public static void generateReturnSalesInvoicePDF(InvoiceData data, String filename) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);

            // Logo (Optional)
            try {
                Image logo = Image.getInstance("CableManagement/cablemanagement/src/main/java/com/cablemanagement/invoice/logo.png");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo not found, continuing without logo");
            }

            // Header
            Paragraph header = new Paragraph("HASEEB WIRE & CABLES", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("SALES RETURN INVOICE", headerFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(5);
            document.add(subHeader);

            Paragraph contact = new Paragraph("Khalil Abad, Amangarh, Nowshera\n0333-4100520 / 0333-9260587\n", regularFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            contact.setSpacingAfter(10);
            document.add(contact);
            
            document.add(new Chunk(new DottedLineSeparator()));
            document.add(Chunk.NEWLINE);

            // Customer & Return Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{6f, 4f});

            // Extract customer name and original invoice from first item
            String customerName = data.getCustomerName();
            String originalInvoice = "N/A";
            
            if (!data.getItems().isEmpty()) {
                String firstItemName = data.getItems().get(0).getName();
                if (firstItemName.contains(" (Return Invoice: ")) {
                    customerName = firstItemName.substring(0, firstItemName.indexOf(" (Return Invoice: "));
                    String returnInvoiceInfo = firstItemName.substring(firstItemName.indexOf(" (Return Invoice: ") + 18);
                    originalInvoice = returnInvoiceInfo.replace(")", "");
                }
            }

            PdfPCell customerCell = new PdfPCell(new Phrase(
                    "Return From:\n" + customerName + "\n" +
                    "Address: Customer Address\n" +
                    "Original Invoice: " + originalInvoice, regularFont));
            customerCell.setBorder(Rectangle.NO_BORDER);
            customerCell.setPaddingBottom(10);

            PdfPCell returnCell = new PdfPCell(new Phrase(
                    "Return Invoice #: " + data.getInvoiceNumber() + "\n" +
                    "Return Date: " + data.getDate() + "\n" +
                    "Processed By: Admin\n" +
                    "Return Reason: Customer Request", regularFont));
            returnCell.setBorder(Rectangle.NO_BORDER);
            returnCell.setPaddingBottom(10);

            infoTable.addCell(customerCell);
            infoTable.addCell(returnCell);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Return Items Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 5, 2, 2.5f, 2.5f});

            String[] headers = {"#", "Item Description", "Return Qty", "Unit Price", "Return Amount"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Add return items to table
            double totalReturnAmount = 0;
            List<Item> items = data.getItems();
            
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                
                // Clean up item name for return
                String itemName = item.getName();
                if (itemName.contains(" (Return Invoice: ")) {
                    itemName = "Returned Item - " + itemName.substring(itemName.indexOf(" (Return Invoice: ") + 18).replace(")", "");
                }
                
                double returnAmount = item.getUnitPrice() * item.getQuantity();
                totalReturnAmount += returnAmount;

                // Row cells
                table.addCell(new Phrase(String.valueOf(i + 1), regularFont));
                table.addCell(new Phrase(itemName, regularFont));
                table.addCell(new Phrase(String.valueOf(item.getQuantity()), regularFont));
                table.addCell(new Phrase(String.format("$%.2f", item.getUnitPrice()), regularFont));
                table.addCell(new Phrase(String.format("$%.2f", returnAmount), regularFont));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Return Summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addSummaryRow(summaryTable, "Total Return Amount:", String.format("$%.2f", totalReturnAmount), boldFont, boldFont);
            addSummaryRow(summaryTable, "Refund Method:", "Store Credit", regularFont, boldFont);
            addSummaryRow(summaryTable, "Processing Fee:", "$0.00", regularFont, boldFont);
            addSummaryRow(summaryTable, "Net Refund:", String.format("$%.2f", totalReturnAmount), boldFont, boldFont);

            document.add(summaryTable);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Return Policy
            Paragraph returnPolicy = new Paragraph("Return Policy:\n" +
                "• Returns accepted within 30 days of purchase\n" +
                "• Items must be in original condition\n" +
                "• Refunds will be processed within 5-7 business days", regularFont);
            returnPolicy.setSpacingBefore(15);
            document.add(returnPolicy);
            document.add(Chunk.NEWLINE);

            // Signature Section
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(20);

            PdfPCell leftSig = new PdfPCell(new Phrase("Customer Signature: ____________________", regularFont));
            leftSig.setBorder(Rectangle.NO_BORDER);
            leftSig.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell rightSig = new PdfPCell(new Phrase("Store Representative: ____________________", regularFont));
            rightSig.setBorder(Rectangle.NO_BORDER);
            rightSig.setHorizontalAlignment(Element.ALIGN_RIGHT);

            signatureTable.addCell(leftSig);
            signatureTable.addCell(rightSig);
            document.add(signatureTable);

            // Footer
            document.add(Chunk.NEWLINE);
            Paragraph thankYou = new Paragraph("THANK YOU FOR YOUR UNDERSTANDING!", headerFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(20);
            document.add(thankYou);

            document.close();
            System.out.println("Return Sales Invoice PDF generated: " + filename);
            
        } catch (Exception e) {
            System.err.println("Error generating Return Sales Invoice PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to add a summary row to the summary table
     */
    private static void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(10);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
