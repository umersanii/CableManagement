package com.cablemanagement.invoice;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Item i1 = new Item("haseeb Cables 7/29", 8, 10400, 10);
        Item i2 = new Item("haseeb Cables 7/36", 4, 15970, 10);
        Item i3 = new Item("Wase Cabel 7/36", 2, 11374, 9);

        InvoiceData data = new InvoiceData(
            InvoiceData.TYPE_SALE, // This is a sales invoice
            "297",
            "2025-06-30",
            "MIYA ELECTRICSTORE",
            "AKORA KHATTAK",
            Arrays.asList(i1, i2, i3),
            1080310.00 // previous balance
        );

        InvoiceGenerator.generatePDF(data, "invoice_297.pdf");
    }
}
