import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.List;

public class InvoiceGenerator {
    public static void generatePDF(InvoiceData data, String filename) {
        try {
            // Create document with A4 size and margins
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Define fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            // Header Section
            Paragraph header = new Paragraph("HASEEB WIRE & CABLES", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph contact = new Paragraph("Khalil Abad, Amangarh, Nowshera\n0923-265138 / 0333-9265587", regularFont);
            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);

            // Invoice Details Table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new int[]{1, 1});

            PdfPCell leftCell = new PdfPCell(new Phrase("Acc #: 155\nCustomer: " + data.getCustomerName() + "\nAddress: " + data.getCustomerAddress(), regularFont));
            leftCell.setBorder(Rectangle.NO_BORDER);
            detailsTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell(new Phrase("Invoice #: " + data.getInvoiceNumber() + "\nDate: " + data.getDate() + "\nOperator: admin", regularFont));
            rightCell.setBorder(Rectangle.NO_BORDER);
            detailsTable.addCell(rightCell);

            document.add(detailsTable);
            document.add(Chunk.NEWLINE);

            // Main Table
            PdfPTable table = new PdfPTable(6); // #, Item, Qty, Unit Price, Discount %, Net Price
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 4, 2, 3, 2, 3});

            // Table Header
            PdfPCell[] headers = {
                new PdfPCell(new Phrase("#", headerFont)),
                new PdfPCell(new Phrase("Item", headerFont)),
                new PdfPCell(new Phrase("Qty", headerFont)),
                new PdfPCell(new Phrase("Unit Price", headerFont)),
                new PdfPCell(new Phrase("Discount %", headerFont)),
                new PdfPCell(new Phrase("Net Price", headerFont))
            };
            for (PdfPCell cell : headers) {
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Data
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

            // Summary Section with Box
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            double totalDiscount = items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice() * i.getDiscountPercent() / 100.0).sum();
            double totalBalance = total + data.getPreviousBalance();
            int totalQuantity = items.stream().mapToInt(Item::getQuantity).sum();

            addSummaryRow(summaryTable, "Bill:", String.format("%.2f", total + totalDiscount), regularFont);
            addSummaryRow(summaryTable, "Discount:", String.format("%.2f", totalDiscount), regularFont);
            addSummaryRow(summaryTable, "Current Net Bill:", String.format("%.2f", total), regularFont);
            addSummaryRow(summaryTable, "Previous Balance:", String.format("%.2f", data.getPreviousBalance()), regularFont);
            addSummaryRow(summaryTable, "Total Balance:", String.format("%.2f", totalBalance), regularFont);
            addSummaryRow(summaryTable, "Other Discount:", "0.00", regularFont);
            addSummaryRow(summaryTable, "Paid:", "0.00", regularFont);
            addSummaryRow(summaryTable, "Net Balance:", String.format("%.2f", totalBalance), regularFont);

            document.add(summaryTable);

            // Total Quantity
            Paragraph totalQty = new Paragraph("Total Quantity: " + totalQuantity, regularFont);
            totalQty.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalQty);

            document.add(Chunk.NEWLINE);
            Paragraph thankYou = new Paragraph("THANK YOU!", titleFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);


            document.close();
            System.out.println("Invoice generated: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}