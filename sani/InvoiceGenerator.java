import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;

public class InvoiceGenerator {
    public static void generatePDF(InvoiceData data, String filename) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("HASEEB WIRE & CABLES", titleFont));
            document.add(new Paragraph("Invoice #: " + data.getInvoiceNumber(), regularFont));
            document.add(new Paragraph("Date: " + data.getDate()));
            document.add(new Paragraph("Customer: " + data.getCustomerName()));
            document.add(new Paragraph("Address: " + data.getCustomerAddress()));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(6); // #, Item, Qty, Unit Price, Discount %, Net Price
            table.setWidthPercentage(100);
            table.addCell("#");
            table.addCell("Item");
            table.addCell("Qty");
            table.addCell("Unit Price");
            table.addCell("Discount %");
            table.addCell("Net Price");

            double total = 0;
            List<Item> items = data.getItems();
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                double amount = item.getUnitPrice() * item.getQuantity();
                double discount = amount * item.getDiscountPercent() / 100.0;
                double net = amount - discount;
                total += net;

                table.addCell(String.valueOf(i + 1));
                table.addCell(item.getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(String.format("%.2f", item.getUnitPrice()));
                table.addCell(String.format("%.1f%%", item.getDiscountPercent()));
                table.addCell(String.format("%.2f", net));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            double totalDiscount = items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice() * i.getDiscountPercent() / 100.0).sum();
            double totalBalance = total + data.getPreviousBalance();

            document.add(new Paragraph("Bill: " + String.format("%.2f", total + totalDiscount)));
            document.add(new Paragraph("Discount: " + String.format("%.2f", totalDiscount)));
            document.add(new Paragraph("Current Net Bill: " + String.format("%.2f", total)));
            document.add(new Paragraph("Previous Balance: " + String.format("%.2f", data.getPreviousBalance())));
            document.add(new Paragraph("Total Balance: " + String.format("%.2f", totalBalance)));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("THANK YOU!", titleFont));

            document.close();
            System.out.println("Invoice generated: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
