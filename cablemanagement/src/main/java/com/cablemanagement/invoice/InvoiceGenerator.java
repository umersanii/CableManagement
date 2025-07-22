package com.cablemanagement.invoice;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

import java.io.FileOutputStream;
import java.util.List;

public class InvoiceGenerator {
    public static void generatePDF(InvoiceData data, String filename) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            // Logo (Optional - comment out if no logo)
            Image logo = Image.getInstance("CableManagement/cablemanagement/src/main/java/com/cablemanagement/invoice/logo.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            // Header
            Paragraph header = new Paragraph("HASEEB WIRE & CABLES", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Purchase Return Invoice", headerFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            Paragraph contact = new Paragraph("Khalil Abad, Amangarh, Nowshera\n0333-4100520 / 0333-9260587\n", regularFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);
            document.add(new Chunk(new DottedLineSeparator()));
            document.add(Chunk.NEWLINE);

            // Supplier & Invoice Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{6f, 4f});

            PdfPCell supplierCell = new PdfPCell(new Phrase(
                    "Supplier: RawMetals Pvt Ltd\n" +
                    "Address: Model Town, Lahore,\nPunjab", regularFont));
            supplierCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell invoiceCell = new PdfPCell(new Phrase(
                    "Invoice #: " + data.getInvoiceNumber() + "\n" +
                    "Date: " + data.getDate() + "\n" +
                    "Original Invoice #: " + data.getInvoiceNumber() + "\n" +
                    "Operator: admin", regularFont));
            invoiceCell.setBorder(Rectangle.NO_BORDER);

            infoTable.addCell(supplierCell);
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

            // Summary Table
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


            PdfPTable summary = new PdfPTable(4);
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

            document.add(summary);
            document.add(Chunk.NEWLINE);

            // Signature Section
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);

            PdfPCell leftSig = new PdfPCell(new Phrase("Signature: ____________________", regularFont));
            leftSig.setBorder(Rectangle.NO_BORDER);
            leftSig.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell rightSig = new PdfPCell(new Phrase("Supplier Signature: ____________________", regularFont));
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
            System.out.println("Invoice generated: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
